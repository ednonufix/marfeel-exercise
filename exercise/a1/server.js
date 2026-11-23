"use strict";

// A1 — Token service (mock downstream, throwaway).
// Issues opaque bearer tokens valid 5 minutes and validates them.
// Stateless apart from the in-memory set of issued tokens + their issue time.
// Zero dependencies: Node built-in modules only.

const http = require("http");
const crypto = require("crypto");

const PORT = parseInt(process.env.PORT || "8080", 10);
const TOKEN_TTL_SECONDS = parseInt(process.env.TOKEN_TTL_SECONDS || "300", 10);

// token -> issuedAtMillis
const issued = new Map();

function isValid(token) {
  const issuedAt = issued.get(token);
  if (issuedAt === undefined) return false;
  return Date.now() - issuedAt <= TOKEN_TTL_SECONDS * 1000;
}

function send(res, status, body) {
  const payload = JSON.stringify(body);
  res.writeHead(status, { "Content-Type": "application/json" });
  res.end(payload);
}

const server = http.createServer((req, res) => {
  const url = new URL(req.url, `http://localhost:${PORT}`);
  const path = url.pathname;

  // POST /a1/token — issue a fresh token. No auth (trivial by design).
  if (req.method === "POST" && path === "/a1/token") {
    const token = crypto.randomBytes(24).toString("hex");
    issued.set(token, Date.now());
    return send(res, 200, { token, expires_in: TOKEN_TTL_SECONDS });
  }

  // GET /a1/validate — used by S1. Authorization: Bearer <token>.
  if (req.method === "GET" && path === "/a1/validate") {
    const auth = req.headers["authorization"] || "";
    const m = /^Bearer\s+(.+)$/i.exec(auth);
    if (!m || !isValid(m[1])) {
      return send(res, 401, { error: "invalid_or_expired_token" });
    }
    return send(res, 200, { valid: true });
  }

  return send(res, 404, { error: "not_found" });
});

server.listen(PORT, () => {
  console.log(`[A1] token service listening on :${PORT} (ttl=${TOKEN_TTL_SECONDS}s)`);
});
