#!/usr/bin/env python3
import argparse
import struct
import threading
import traceback
from dataclasses import dataclass
from typing import Optional

import stomp


GMP_STATUS_PREFIX = "GMP.STATUS."

msg=0
@dataclass
class StatusItem:
    name: str
    value: object
    timestamp_ms: int
    status_type: str
    alarm_severity: Optional[str] = None
    alarm_cause: Optional[str] = None
    alarm_message: Optional[str] = None


class ByteReader:
    def __init__(self, data: bytes) -> None:
        self._data = data
        self._idx = 0

    def _read(self, count: int) -> bytes:
        if self._idx + count > len(self._data):
            raise ValueError("message truncated")
        chunk = self._data[self._idx : self._idx + count]
        self._idx += count
        return chunk

    def read_ubyte(self) -> int:
        return self._read(1)[0]

    def read_bool(self) -> bool:
        return self.read_ubyte() != 0

    def read_int(self) -> int:
        return struct.unpack(">i", self._read(4))[0]

    def read_long(self) -> int:
        return struct.unpack(">q", self._read(8))[0]

    def read_float(self) -> float:
        return struct.unpack(">f", self._read(4))[0]

    def read_double(self) -> float:
        return struct.unpack(">d", self._read(8))[0]

    def read_utf(self) -> str:
        length = struct.unpack(">H", self._read(2))[0]
        return decode_modified_utf8(self._read(length))


def decode_modified_utf8(data: bytes) -> str:
    # Java DataInputStream's modified UTF-8 (sufficient for ASCII/UTF-8 status names).
    out = []
    i = 0
    while i < len(data):
        b = data[i]
        if b <= 0x7F:
            out.append(chr(b))
            i += 1
        elif (b & 0xE0) == 0xC0:
            b2 = data[i + 1]
            if b == 0xC0 and b2 == 0x80:
                out.append("\u0000")
            else:
                out.append(chr(((b & 0x1F) << 6) | (b2 & 0x3F)))
            i += 2
        else:
            b2 = data[i + 1]
            b3 = data[i + 2]
            out.append(chr(((b & 0x0F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F)))
            i += 3
    return "".join(out)


def parse_status_item(data: bytes) -> Optional[StatusItem]:

    if not data:
        return None
    r = ByteReader(data)
    type_code = r.read_ubyte()
    name = r.read_utf()

    alarm = False
    if type_code in (0, 10):
        value = r.read_int()
        status_type = "INT"
        alarm = type_code >= 10
    elif type_code in (1, 11):
        value = r.read_double()
        status_type = "DOUBLE"
        alarm = type_code >= 10
    elif type_code in (2, 12):
        value = r.read_float()
        status_type = "FLOAT"
        alarm = type_code >= 10
    elif type_code in (3, 13):
        value = r.read_utf()
        status_type = "STRING"
        alarm = type_code >= 10
    elif type_code == 20:
        health_code = r.read_int()
        value = {0: "GOOD", 1: "WARNING", 2: "BAD"}.get(health_code, "DEFAULT")
        status_type = "HEALTH"
    else:
        raise ValueError(f"unknown status type code {type_code}")

    timestamp_ms = r.read_long()

    if not alarm:
        return StatusItem(name=name, value=value, timestamp_ms=timestamp_ms, status_type=status_type)

    severity_code = r.read_ubyte()
    cause_code = r.read_ubyte()
    has_message = r.read_bool()
    alarm_message = r.read_utf() if has_message else None

    severity = {0: "ALARM_OK", 1: "ALARM_WARNING", 2: "ALARM_FAILURE"}.get(severity_code, "UNKNOWN")
    cause = {
        0: "ALARM_CAUSE_OK",
        1: "ALARM_CAUSE_HIHI",
        2: "ALARM_CAUSE_HI",
        3: "ALARM_CAUSE_LOLO",
        4: "ALARM_CAUSE_LO",
        5: "ALARM_CAUSE_OTHER",
    }.get(cause_code, "UNKNOWN")

    return StatusItem(
        name=name,
        value=value,
        timestamp_ms=timestamp_ms,
        status_type=status_type,
        alarm_severity=severity,
        alarm_cause=cause,
        alarm_message=alarm_message,
    )


class StatusListener(stomp.ConnectionListener):
    def __init__(self, stop_event: threading.Event) -> None:
        self._stop_event = stop_event

    def on_message(self, frame) -> None:
        try:
            body = frame.body
            if isinstance(body, str):
                body = body.encode("latin-1", errors="replace")
            item = parse_status_item(body)
            if item is None:
                print("received: <empty>")
                return
            if item.alarm_severity:
                print(
                    f"received: name={item.name} value={item.value} type={item.status_type} "
                    f"timestamp_ms={item.timestamp_ms} alarm={item.alarm_severity}/{item.alarm_cause} "
                    f"message={item.alarm_message}"
                )
            else:
                print(
                    f"received: name={item.name} value={item.value} type={item.status_type} "
                    f"timestamp_ms={item.timestamp_ms}"
                )
        except Exception:
            traceback.print_exc()

    def on_error(self, frame) -> None:
        print(f"error: {frame.body}")

    def on_disconnected(self) -> None:
        self._stop_event.set()


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Subscribe to GMP status updates via STOMP.")
    parser.add_argument("--host", default="127.0.0.1", help="Broker host")
    parser.add_argument("--port", type=int, default=61613, help="STOMP port")
    parser.add_argument("--user", default="", help="Username (optional)")
    parser.add_argument("--password", default="", help="Password (optional)")
    parser.add_argument(
        "--status-name",
        default="gpi:status1",
        help="Status item name (builds destination GMP.STATUS.<name>)",
    )
    parser.add_argument(
        "--destination",
        default="",
        help="Override destination, e.g. /topic/GMP.STATUS.gpi:status1",
    )
    return parser


def main() -> None:
    args = build_parser().parse_args()
    destination = args.destination or f"/topic/{GMP_STATUS_PREFIX}{args.status_name}"

    stop_event = threading.Event()
    try:
        conn = stomp.Connection([(args.host, args.port)], auto_decode=False)
    except TypeError:
        conn = stomp.Connection([(args.host, args.port)])
    conn.set_listener("", StatusListener(stop_event))
    conn.connect(args.user, args.password, wait=True)
    conn.subscribe(destination=destination, id=1, ack="auto")

    print(f"listening on {destination} (ctrl-c to quit)")
    try:
        stop_event.wait()
    except KeyboardInterrupt:
        pass
    finally:
        conn.disconnect()


if __name__ == "__main__":
    main()
