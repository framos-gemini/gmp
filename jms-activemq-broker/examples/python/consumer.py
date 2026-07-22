#!/usr/bin/env python3
import argparse
import threading

import stomp


class PrintListener(stomp.ConnectionListener):
    def __init__(
        self,
        stop_event: threading.Event,
        conn: stomp.Connection,
        init_handler: bool,
        response: str,
        error_message: str,
    ) -> None:
        self._stop_event = stop_event
        self._conn = conn
        self._init_handler = init_handler
        self._response = response
        self._error_message = error_message

    def on_message(self, frame) -> None:
        print(f"received: {frame.body}")
        print(f"headers: {frame.headers}")
        if self._init_handler:
            self._handle_init(frame)

    def on_error(self, frame) -> None:
        print(f"error: {frame.body}")

    def on_disconnected(self) -> None:
        self._stop_event.set()

    def _handle_init(self, frame) -> None:
        headers = frame.headers or {}
        reply_to = headers.get("reply-to") or headers.get("JMSReplyTo")
        correlation_id = headers.get("correlation-id") or headers.get("JMSCorrelationID")

        if not reply_to:
            print("missing reply-to header; cannot send INIT response")
            return

        send_headers = {
            "content-type": "text/plain",
            "GMP_HANDLER_RESPONSE": self._response,
        }
        if self._response == "ERROR":
            send_headers["GMP_HANDLER_RESPONSE_ERROR"] = self._error_message
        if correlation_id:
            send_headers["correlation-id"] = correlation_id

        self._conn.send(destination=reply_to, body="", headers=send_headers)
        print(f"sent INIT response {self._response} to {reply_to}")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Receive STOMP messages from ActiveMQ.")
    parser.add_argument("--host", default="127.0.0.1", help="Broker host")
    parser.add_argument("--port", type=int, default=61613, help="STOMP port")
    parser.add_argument("--user", default="", help="Username (optional)")
    parser.add_argument("--password", default="", help="Password (optional)")
    parser.add_argument(
        "--destination",
        default="/queue/gmp.example",
        help="STOMP destination, e.g. /queue/foo or /topic/bar",
    )
    parser.add_argument(
        "--init-handler",
        action="store_true",
        help="Handle GMP INIT commands and reply with a handler response",
    )
    parser.add_argument(
        "--response",
        default="COMPLETED",
        help="Handler response to send (COMPLETED, STARTED, ACCEPTED, NOANSWER, ERROR)",
    )
    parser.add_argument(
        "--error-message",
        default="",
        help="Error message to send when --response=ERROR",
    )
    return parser


def main() -> None:
    args = build_parser().parse_args()
    args.response = args.response.upper()
    stop_event = threading.Event()
    conn = stomp.Connection([(args.host, args.port)])
    conn.set_listener(
        "",
        PrintListener(stop_event, conn, args.init_handler, args.response, args.error_message),
    )
    conn.connect(args.user, args.password, wait=True)
    destination = args.destination
    if args.init_handler and args.destination == "/queue/gmp.example":
        destination = "/topic/GMP.SC.INIT"
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
