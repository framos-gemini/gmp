import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import gearSvg from "./assets/gear.svg";

type StatusPayload = {
  name: string;
  value: string | number | null;
  status_type: string;
  timestamp_ms: number;
  alarm_severity?: string | null;
  alarm_cause?: string | null;
  alarm_message?: string | null;
};

type LogEntry = {
  id: string;
  at: string;
  payload: StatusPayload | { type: "system"; message: string };
};

const defaultHost = window.location.hostname || "127.0.0.1";
const defaultPort = "8000";
const defaultPath = "/ws/status";

const buildWsUrl = (host: string, port: string, path: string) => {
  const prefix = window.location.protocol === "https:" ? "wss" : "ws";
  return `${prefix}://${host}:${port}${path.startsWith("/") ? path : `/${path}`}`;
};

const formatTimestamp = (value: number) =>
  new Date(value).toLocaleTimeString();

const toNumber = (value: StatusPayload["value"]) => {
  if (typeof value === "number") {
    return value;
  }
  if (typeof value === "string") {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }
  return null;
};

const toRotation = (value: StatusPayload["value"]) => {
  const num = toNumber(value);
  if (num === null) {
    return 0;
  }
  const clamped = Math.max(0, Math.min(1000, num));
  return (clamped / 1000) * 360;
};

const makeId = () =>
  typeof crypto !== "undefined" && "randomUUID" in crypto
    ? crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(16).slice(2)}`;

const toLogEntry = (payload: LogEntry["payload"]): LogEntry => ({
  id: makeId(),
  at: new Date().toLocaleTimeString(),
  payload,
});

export default function App() {
  const [host, setHost] = useState(defaultHost);
  const [port, setPort] = useState(defaultPort);
  const [path, setPath] = useState(defaultPath);
  const [connected, setConnected] = useState(false);
  const [log, setLog] = useState<LogEntry[]>([]);
  const [lastPayload, setLastPayload] = useState<StatusPayload | null>(null);
  const wsRef = useRef<WebSocket | null>(null);
  const keepAliveRef = useRef<number | null>(null);

  const wsUrl = useMemo(() => buildWsUrl(host, port, path), [host, port, path]);

  const pushLog = useCallback((payload: LogEntry["payload"]) => {
    setLog((prev) => [toLogEntry(payload), ...prev].slice(0, 100));
  }, []);

  const connect = useCallback(() => {
    if (wsRef.current) {
      wsRef.current.close();
    }
    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    ws.onopen = () => {
      setConnected(true);
      pushLog({ type: "system", message: `Connected to ${wsUrl}` });
      keepAliveRef.current = window.setInterval(() => {
        ws.send("ping");
      }, 30000);
    };

    ws.onmessage = (event) => {
      try {
        const payload = JSON.parse(event.data) as StatusPayload;
        setLastPayload(payload);
        pushLog(payload);
      } catch (error) {
        pushLog({
          type: "system",
          message: `Invalid payload: ${String(error)}`,
        });
      }
    };

    ws.onerror = () => {
      pushLog({ type: "system", message: "WebSocket error" });
    };

    ws.onclose = () => {
      setConnected(false);
      pushLog({ type: "system", message: "Disconnected" });
      if (keepAliveRef.current) {
        window.clearInterval(keepAliveRef.current);
        keepAliveRef.current = null;
      }
    };
  }, [pushLog, wsUrl]);

  const disconnect = useCallback(() => {
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }
  }, []);

  useEffect(() => {
    return () => {
      disconnect();
    };
  }, [disconnect]);

  return (
    <div className="page">
      <header className="hero">
        <div>
          <p className="eyebrow">GMP Status Stream</p>
          <h1>Live status data over STOMP → WebSocket.</h1>
          <p className="subtitle">
            Connect to the FastAPI bridge and watch `gpi:status1` updates flow
            in real time.
          </p>
        </div>
        <div className="status-pill">
          <span className={`dot ${connected ? "online" : "offline"}`} />
          {connected ? "Live" : "Idle"}
        </div>
      </header>

      <section className="panel grid">
        <div className="card">
          <h2>Connection</h2>
          <div className="form">
            <label>
              Host
              <input
                value={host}
                onChange={(event) => setHost(event.target.value)}
              />
            </label>
            <label>
              Port
              <input
                value={port}
                onChange={(event) => setPort(event.target.value)}
              />
            </label>
            <label>
              WS Path
              <input
                value={path}
                onChange={(event) => setPath(event.target.value)}
              />
            </label>
          </div>
          <div className="buttons">
            <button onClick={connect} className="primary">
              Connect
            </button>
            <button onClick={disconnect} className="ghost">
              Disconnect
            </button>
          </div>
          <p className="hint">Current: {wsUrl}</p>
        </div>

        <div className="card highlight">
          <h2>Latest status</h2>
          {lastPayload ? (
            <div className="payload">
              <div>
                <span>Status</span>
                <strong>{lastPayload.name}</strong>
              </div>
              <div>
                <span>Value</span>
                <strong>{String(lastPayload.value)}</strong>
              </div>
              <div>
                <span>Type</span>
                <strong>{lastPayload.status_type}</strong>
              </div>
              <div>
                <span>Timestamp</span>
                <strong>{formatTimestamp(lastPayload.timestamp_ms)}</strong>
              </div>
              {lastPayload.alarm_severity && (
                <div>
                  <span>Alarm</span>
                  <strong>
                    {lastPayload.alarm_severity}/{lastPayload.alarm_cause}
                  </strong>
                </div>
              )}
              <div className="gear-row">
                <span>Position</span>
                <div className="gear-wrap">
                  <div
                    className="gear"
                    style={{ transform: `rotate(${toRotation(lastPayload.value)}deg)` }}
                  >
                    <svg viewBox="0 0 200 200" aria-hidden="true">
                      <defs>
                        <linearGradient id="gearFill" x1="0" x2="1">
                          <stop offset="0%" stopColor="#d94b2b" />
                          <stop offset="100%" stopColor="#f2a14e" />
                        </linearGradient>
                      </defs>
                      <g>
                        <circle cx="100" cy="100" r="70" fill="url(#gearFill)" />
                        <circle cx="100" cy="100" r="40" fill="#fff8ef" />
                        {Array.from({ length: 12 }).map((_, idx) => (
                          <rect
                            key={idx}
                            x="92"
                            y="10"
                            width="16"
                            height="28"
                            rx="6"
                            fill="#c03a1b"
                            transform={`rotate(${idx * 30} 100 100)`}
                          />
                        ))}
                      </g>
                    </svg>
                  </div>
                  <div className="gear-readout">
                    {toNumber(lastPayload.value) ?? "--"} / 1000
                  </div>
                </div>
              </div>
              <div className="gear-row">
                <span>Position (SVG file)</span>
                <div className="gear-wrap">
                  <img
                    className="gear gear-img"
                    src={gearSvg}
                    alt="Gear"
                    style={{ transform: `rotate(${toRotation(lastPayload.value)}deg)` }}
                  />
                  <div className="gear-readout">
                    {toNumber(lastPayload.value) ?? "--"} / 1000
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <p className="empty">No status updates yet.</p>
          )}
        </div>
      </section>

      <section className="panel">
        <div className="card log">
          <h2>Stream log</h2>
          <div className="log-list">
            {log.length === 0 && (
              <p className="empty">Waiting for messages…</p>
            )}
            {log.map((entry) => (
              <div key={entry.id} className="log-item">
                <div className="time">{entry.at}</div>
                {"type" in entry.payload ? (
                  <div className="system">{entry.payload.message}</div>
                ) : (
                  <div className="message">
                    <span>{entry.payload.name}</span>
                    <span className="mono">{String(entry.payload.value)}</span>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
