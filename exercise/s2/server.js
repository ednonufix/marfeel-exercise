"use strict";

// S2 — Fast subscription service (mock downstream, throwaway).
// - Static token auth via X-Api-Key header; mismatch -> 401.
// - Fast (effectively immediate).
// - CORE TRAP: every business failure is 200 + an error body, never an
//   error status. Field-match gates first ("invalid item"), then per-user
//   branch (U1 -> "user not eligible", U2 -> success).
// - Stateless: GET always returns the full fixed seed; subscribe mutates nothing.
// Zero dependencies: Node built-in modules only.

const http = require("http");

const PORT = parseInt(process.env.PORT || "8080", 10);
const API_KEY = process.env.S2_API_KEY || "s2-secret-key";

// Fixed in-memory catalog seed (S2's own, flatter shape — different from S1).
const CATALOG = {
  items: [
    { item: { itemId: "S2-100" } },
    { item: { itemId: "S2-200" } },
    { item: { itemId: "S2-300" } }
  ]
};

function send(res, status, body) {
  res.writeHead(status, { "Content-Type": "application/json" });
  res.end(JSON.stringify(body));
}

function readBody(req) {
  return new Promise((resolve) => {
    let data = "";
    req.on("data", (c) => (data += c));
    req.on("end", () => {
      try {
        resolve(data ? JSON.parse(data) : {});
      } catch {
        resolve(null);
      }
    });
  });
}

function userOf(ref) {
  const s = String(ref || "").toUpperCase();
  if (s.includes("U1")) return "U1";
  if (s.includes("U2")) return "U2";
  if (s.includes("U3")) return "U3";
  return "UNKNOWN";
}

const server = http.createServer(async (req, res) => {
  const path = new URL(req.url, `http://localhost:${PORT}`).pathname;

  // Static-token auth on both endpoints.
  if ((req.headers["x-api-key"] || "") !== API_KEY) {
    return send(res, 401, { error: "invalid_api_key" });
  }

  // GET /s2/items — full catalog every time.
  if (req.method === "GET" && path === "/s2/items") {
    return send(res, 200, CATALOG);
  }

  // POST /s2/subscribe — all outcomes are HTTP 200.
  if (req.method === "POST" && path === "/s2/subscribe") {
    const body = await readBody(req);
    if (!body) return send(res, 400, { error: "bad_json" });

    const itemId = body?.item?.itemId;
    const matches = CATALOG.items.some((e) => e.item.itemId === itemId);
    if (!matches) {
      return send(res, 200, { status: "ERROR", reason: "invalid item" });
    }

    const user = userOf(body?.user);
    if (user === "U1") {
      return send(res, 200, { status: "ERROR", reason: "user not eligible" });
    }
    // U2 (and anything else that matched) -> success.
    return send(res, 200, {
      status: "OK",
      subscriptionId: `s2-${itemId}-${Date.now()}`
    });
  }

  return send(res, 404, { error: "not_found" });
});

server.listen(PORT, () => {
  console.log(`[S2] fast service on :${PORT} (apiKey=${API_KEY})`);
});
