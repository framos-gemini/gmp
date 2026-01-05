#!/usr/bin/env python3
import argparse
import time

import stomp


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Send STOMP messages to ActiveMQ.")
    parser.add_argument("--host", default="127.0.0.1", help="Broker host")
    parser.add_argument("--port", type=int, default=61613, help="STOMP port")
    parser.add_argument("--user", default="", help="Username (optional)")
    parser.add_argument("--password", default="", help="Password (optional)")
    parser.add_argument(
        "--destination",
        default="/queue/gmp.example",
        help="STOMP destination, e.g. /queue/foo or /topic/bar",
    )
    parser.add_argument("--message", default="hello from producer", help="Message body")
    parser.add_argument("--count", type=int, default=1, help="Number of messages")
    parser.add_argument("--delay", type=float, default=0.0, help="Delay between messages (s)")
    return parser


def main() -> None:
    args = build_parser().parse_args()
    conn = stomp.Connection([(args.host, args.port)])
    conn.connect(args.user, args.password, wait=True)

    for i in range(args.count):
        body = args.message if args.count == 1 else f"{args.message} #{i + 1}"
        conn.send(destination=args.destination, body=body)
        print(f"sent: {body}")
        if args.delay:
            time.sleep(args.delay)

    conn.disconnect()


if __name__ == "__main__":
    main()
