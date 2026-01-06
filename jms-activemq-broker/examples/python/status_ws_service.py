#!/usr/bin/env python3
import asyncio
import json
import os
import sys
import threading
import traceback
from pathlib import Path
from typing import Any, Dict, Set

import stomp
from fastapi import FastAPI, WebSocket, WebSocketDisconnect

_HERE = Path(__file__).resolve().parent
if str(_HERE) not in sys.path:
    sys.path.insert(0, str(_HERE))
from status_subscriber import parse_status_item


STOMP_HOST = os.getenv("STOMP_HOST", "127.0.0.1")
STOMP_PORT = int(os.getenv("STOMP_PORT", "61613"))
STOMP_USER = os.getenv("STOMP_USER", "")
STOMP_PASSWORD = os.getenv("STOMP_PASSWORD", "")
STATUS_NAME = os.getenv("STATUS_NAME", "gpi:status1")
DESTINATION = os.getenv("DESTINATION", f"/topic/GMP.STATUS.{STATUS_NAME}")


class WebSocketHub:
    def __init__(self) -> None:
        self._clients: Set[WebSocket] = set()
        self._lock = asyncio.Lock()

    async def connect(self, ws: WebSocket) -> None:
        await ws.accept()
        async with self._lock:
            self._clients.add(ws)

    async def disconnect(self, ws: WebSocket) -> None:
        async with self._lock:
            self._clients.discard(ws)

    async def broadcast(self, payload: Dict[str, Any]) -> None:
        message = json.dumps(payload)
        async with self._lock:
            clients = list(self._clients)
        for ws in clients:
            try:
                await ws.send_text(message)
            except Exception:
                await self.disconnect(ws)


class StompStatusListener(stomp.ConnectionListener):
    def __init__(self, loop: asyncio.AbstractEventLoop, hub: WebSocketHub) -> None:
        self._loop = loop
        self._hub = hub

    def on_message(self, frame) -> None:
        try:
            body = frame.body
            if isinstance(body, str):
                body = body.encode("latin-1", errors="replace")
            item = parse_status_item(body)
            if item is None:
                payload = {"type": "empty"}
            else:
                payload = {
                    "name": item.name,
                    "value": item.value,
                    "status_type": item.status_type,
                    "timestamp_ms": item.timestamp_ms,
                    "alarm_severity": item.alarm_severity,
                    "alarm_cause": item.alarm_cause,
                    "alarm_message": item.alarm_message,
                }
            asyncio.run_coroutine_threadsafe(self._hub.broadcast(payload), self._loop)
        except Exception:
            traceback.print_exc()

    def on_error(self, frame) -> None:
        print(f"stomp error: {frame.body}")


app = FastAPI()
hub = WebSocketHub()


@app.on_event("startup")
def startup() -> None:
    loop = asyncio.get_event_loop()
    conn = stomp.Connection([(STOMP_HOST, STOMP_PORT)], auto_decode=False)
    conn.set_listener("", StompStatusListener(loop, hub))
    conn.connect(STOMP_USER, STOMP_PASSWORD, wait=True)
    conn.subscribe(destination=DESTINATION, id=1, ack="auto")
    app.state.stomp_conn = conn
    print(f"STOMP subscribed to {DESTINATION}")


@app.on_event("shutdown")
def shutdown() -> None:
    conn = getattr(app.state, "stomp_conn", None)
    if conn is not None:
        conn.disconnect()


@app.websocket("/ws/status")
async def status_ws(ws: WebSocket) -> None:
    await hub.connect(ws)
    try:
        while True:
            await ws.receive_text()
    except WebSocketDisconnect:
        await hub.disconnect(ws)
