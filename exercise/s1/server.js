"use strict";

// S1 — Slow subscription service (mock downstream, throwaway).
// - Bearer auth, validated by calling A1 GET /a1/validate.
// - POST sleeps ~60s (configurable) before responding.
// - Outcome: field-match gates first (else 422), then per-user branch
//   (U1 -> 200 ACTIVE, U2 -> 500). Statelessness: GET always returns the
//   full fixed seed; subscribing mutates nothing.
// Zero dependencies: Node built-in modules only.

const http = require("http");

const PORT = parseInt(process.env.PORT || "8080", 10);
const A1_URL = process.env.A1_URL || "http://a1:8080";
const LATENCY_MS = parseInt(process.env.S1_LATENCY_MS || "60000", 10);

// Fixed in-memory product catalog seed (S1's own nested shape).
const CATALOG = {
  products: [
    { product: { code: "S1-A", subcode: "X1" } },
    { product: { code: "S1-B", subcode: "X2" } },
    { product: { code: "S1-C", subcode: "X3" } }
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
        resolve(null); // signal bad JSON
      }
    });
  });
}

// Validate the incoming bearer token by asking A1.
function validateWithA1(authHeader) {
  return new Promise((resolve) => {
    if (!authHeader) return resolve(false);
    const u = new URL("/a1/validate", A1_URL);
    const r = http.request(
      { method: "GET", hostname: u.hostname, port: u.port, path: u.pathname, headers: { Authorization: authHeader } },
      (resp) => {
        resp.resume();
        resolve(resp.statusCode === 200);
      }
    );
    r.on("error", () => resolve(false));
    r.end();
  });
}

// Identify the user from a ref string (registry sends u1/u2/u3).
function userOf(ref) {
  const s = String(ref || "").toUpperCase();
  if (s.includes("U1")) return "U1";
  if (s.includes("U2")) return "U2";
  if (s.includes("U3")) return "U3";
  return "UNKNOWN";
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

const server = http.createServer(async (req, res) => {
  const path = new URL(req.url, `http://localhost:${PORT}`).pathname;
  const auth = req.headers["authorization"];

  // Auth applies to both endpoints; S1 delegates validation to A1.
  const ok = await validateWithA1(auth);
  if (!ok) return send(res, 401, { error: "invalid_or_expired_token" });

  // GET /s1/products — full product catalog every time, fast.
  if (req.method === "GET" && path === "/s1/products") {
    return send(res, 200, CATALOG);
  }

  // POST /s1/subscribe — the slow path (one product per call).
  if (req.method === "POST" && path === "/s1/subscribe") {
    const body = await readBody(req);
    if (!body) return send(res, 400, { error: "bad_json" });

    // Auth is validated fast (above); the latency models the work.
    await sleep(LATENCY_MS);

    const code = body?.product?.code;
    const subcode = body?.product?.subcode;
    const matches = CATALOG.products.some(
      (e) => e.product.code === code && e.product.subcode === subcode
    );
    if (!matches) {
      return send(res, 422, { error: "no_valid_subscription" });
    }

    const user = userOf(body?.account?.ref);
    if (user === "U2") {
      return send(res, 500, { error: "internal_error" }); // transport-level failure
    }
    // U1 (and anything else that matched) -> success.
    return send(res, 200, {
      subscriptionId: `s1-${code}-${subcode}-${Date.now()}`,
      state: "ACTIVE"
    });
  }

  return send(res, 404, { error: "not_found" });
});

server.listen(PORT, () => {
  console.log(`[S1] slow service on :${PORT} (latency=${LATENCY_MS}ms, a1=${A1_URL})`);
});
