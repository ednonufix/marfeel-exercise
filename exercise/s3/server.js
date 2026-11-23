"use strict";

// S3 — Product metrics service (mock downstream, throwaway).
// Given a product identity — in that product's OWN shape (S1: code+subcode,
// S2: itemId) — returns per-product metrics: a subscriber count and an IBAN.
// One product per call:
//   GET /s3/metrics?service=S1&code=S1-A&subcode=X1   (or ?service=S2&itemId=...)
// Auth: a STATIC bearer token. It looks like S1's bearer, but — unlike S1's —
// it is NOT issued by A1; it's a fixed configured value.
// Stateless: fixed in-memory seed; returns the same numbers every time.
// Zero dependencies: Node built-in modules only.

const http = require("http");

const PORT = parseInt(process.env.PORT || "8080", 10);
const S3_TOKEN = process.env.S3_TOKEN || "s3-static-token";
const LATENCY_MS = parseInt(process.env.S3_LATENCY_MS || "150", 10);

// Fixed metrics seed, keyed by the product's identity.
const METRICS = {
  "S1|S1-A|X1": { subscribers: 1280, iban: "DE89370400440532013000" },
  "S1|S1-B|X2": { subscribers: 542, iban: "NL91ABNA0417164300" },
  "S1|S1-C|X3": { subscribers: 8731, iban: "FR1420041010050500013M02606" },
  "S2|S2-100": { subscribers: 17, iban: "ES9121000418450200051332" },
  "S2|S2-200": { subscribers: 4096, iban: "GB29NWBK60161331926819" },
  "S2|S2-300": { subscribers: 333, iban: "IT60X0542811101000000123456" }
};

function send(res, status, body) {
  res.writeHead(status, { "Content-Type": "application/json" });
  res.end(JSON.stringify(body));
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// Normalise an incoming identity to a clean, service-native shape.
function identityOf(p) {
  const service = String(p.service || "").toUpperCase();
  if (service === "S1") return { service: "S1", code: p.code, subcode: p.subcode || "" };
  if (service === "S2") return { service: "S2", itemId: p.itemId };
  return { service };
}

function keyOf(id) {
  if (id.service === "S1") return `S1|${id.code}|${id.subcode || ""}`;
  if (id.service === "S2") return `S2|${id.itemId}`;
  return null;
}

// Echo the identity back alongside its metrics (zeros/null when unknown).
function metricFor(p) {
  const id = identityOf(p);
  const m = METRICS[keyOf(id)];
  return { ...id, subscribers: m ? m.subscribers : 0, iban: m ? m.iban : null };
}

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, `http://localhost:${PORT}`);
  const path = url.pathname;

  // Static-bearer auth on every endpoint (NOT an A1-issued token).
  const m = /^Bearer\s+(.+)$/i.exec(req.headers["authorization"] || "");
  if (!m || m[1] !== S3_TOKEN) {
    return send(res, 401, { error: "invalid_token" });
  }

  await sleep(LATENCY_MS);

  // GET /s3/metrics?service=S1&code=..&subcode=..   |   ?service=S2&itemId=..
  if (req.method === "GET" && path === "/s3/metrics") {
    const id = identityOf({
      service: url.searchParams.get("service"),
      code: url.searchParams.get("code"),
      subcode: url.searchParams.get("subcode"),
      itemId: url.searchParams.get("itemId")
    });
    if (!METRICS[keyOf(id)]) {
      return send(res, 404, { error: "unknown_product" });
    }
    return send(res, 200, metricFor(id));
  }

  return send(res, 404, { error: "not_found" });
});

server.listen(PORT, () => {
  console.log(`[S3] metrics service on :${PORT} (latency=${LATENCY_MS}ms, static bearer)`);
});
