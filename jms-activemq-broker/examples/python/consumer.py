#!/usr/bin/env python3
import argparse
import threading

import stomp


class PrintListener(stomp.ConnectionListener):
    def __init__(self, stop_event: threading.Event) -> None:
        self._stop_event = stop_event

    def on_message(self, frame) -> None:
        print(f"received: {frame.body}")

    def on_error(self, frame) -> None:
        print(f"error: {frame.body}")

    def on_disconnected(self) -> None:
        self._stop_event.set()


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
    return parser


def main() -> None:
    args = build_parser().parse_args()
    stop_event = threading.Event()
    conn = stomp.Connection([(args.host, args.port)])
    conn.set_listener("", PrintListener(stop_event))
    conn.connect(args.user, args.password, wait=True)
    conn.subscribe(destination=args.destination, id=1, ack="auto")

    print(f"listening on {args.destination} (ctrl-c to quit)")
    try:
        stop_event.wait()
    except KeyboardInterrupt:
        pass
    finally:
        conn.disconnect()


if __name__ == "__main__":
    main()
