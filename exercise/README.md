# Mock downstreams + database

This folder provides everything the **Subscription Registry** exercise integrates
against. You build the registry; these services are given to you and must not be
changed. They are deliberately dumb, stateless, and deterministic.

| Service | Role                                            | Default host port | Auth                     |
|---------|-------------------------------------------------|-------------------|--------------------------|
| `a1`    | Token service — issues/validates bearer tokens  | `8081`            | none (issue)             |
| `s1`    | **Slow** subscription service (~60s POST)       | `8082`            | bearer, validated via A1 |
| `s2`    | **Fast** subscription service                   | `8083`            | static `X-Api-Key`       |
| `s3`    | Product **metrics** service (subscribers, IBAN) | `8084`            | static `Bearer`          |
| `db`    | MySQL 8 for your registry                       | `3307`            | user/pass below          |

## Run

```bash
docker compose up --build      # add -d to detach
docker compose down -v         # stop and wipe the DB volume
```

All ports and knobs live in [`.env`](./.env) and are read by
`docker-compose.yml` — edit them, no rebuild needed. Containers always listen on
`8080` internally; only the published host port changes. Inside the compose
network, S1 reaches A1 at `http://a1:8080` (set via `A1_URL`).

> **Tip:** while developing, drop `S1_LATENCY_MS` to e.g. `500` so you aren't
> waiting 60s per call:
> `S1_LATENCY_MS=500 docker compose up --build`

Database connection (from your registry running on the host):
`jdbc:mysql://localhost:3307/registry` — user `registry`, password `registry`
(root password `root`; all configurable in `.env`). MySQL takes a few seconds to
initialize on first boot — wait for the `db` container to report healthy/ready
before pointing your registry at it.

## Contracts (authoritative — the exercise brief is in `../test.md`)

### A1 — token service

- `POST /a1/token` → `200 {"token","expires_in":300}`. Each call is a fresh
  token; older tokens stay valid until their own 5-min expiry. No rotation/revocation.
- `GET /a1/validate` with `Authorization: Bearer <token>` → `200` if issued in
  the last 5 min, else `401`.

### S1 — slow service (bearer auth, validated by calling A1)

- `GET /s1/products` → fixed product catalog (full list every time, nested shape,
  under a `"products"` key).
- `POST /s1/subscribe` body `{"account":{"ref":"<user>"},"product":{"code","subcode"}}`:
    - sleeps `S1_LATENCY_MS` (~60s), then
    - fields don't match catalog → `422`
    - match + **U1** → `200 {"subscriptionId","state":"ACTIVE"}`
    - match + **U2** → `500`
- Missing/invalid/expired bearer → `401` (fast, before the sleep).

### S2 — fast service (static `X-Api-Key`)

- `GET /s2/items` → fixed catalog (full list every time, different/flatter shape).
- `POST /s2/subscribe` body `{"user":"<user>","item":{"itemId":"..."}}`
  — **every business outcome is HTTP `200`**:
    - itemId not in catalog → `200 {"status":"ERROR","reason":"invalid item"}`
    - match + **U2** → `200 {"status":"OK","subscriptionId":...}`
    - match + **U1** → `200 {"status":"ERROR","reason":"user not eligible"}`
- Wrong/missing key → `401`.

### S3 — product metrics service (static `Bearer`)

Returns per-product metrics — `subscribers` (count) and `iban` — keyed by the
product's identity **in that service's own shape** (S1: `code`+`subcode`;
S2: `itemId`). Stateless fixed seed; same numbers every time.

- `GET /s3/metrics?service=S1&code=S1-A&subcode=X1`
  (or `?service=S2&itemId=S2-100`) →
  `200 {"service","code","subcode","subscribers","iban"}`. Unknown product → `404`.
- Each request returns a single product's metrics.
- **Auth:** `Authorization: Bearer <S3_TOKEN>` — a **static** configured token.
  It is a Bearer token like S1's, but **not** issued by A1. Missing/wrong → `401`.
- **Latency:** `S3_LATENCY_MS` (default 150ms) on every call.

### Identifying users

The mocks read the user from the request (`account.ref` for S1, `user` for S2)
and look for `U1` / `U2` / `U3` (case-insensitive substring). Your registry must
send the user id downstream — pick a ref that contains the user id.

## Quick smoke test

```bash
# Catalogs
TOK=$(curl -s -X POST localhost:8081/a1/token | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
curl -s -H "Authorization: Bearer $TOK" localhost:8082/s1/products
curl -s -H "X-Api-Key: s2-secret-key" localhost:8083/s2/items

# Valid S1 subscribe — U1 + a catalog product (expect 200 ACTIVE after ~60s;
# lower S1_LATENCY_MS while developing). U2 with the same body returns 500.
curl -s -H "Authorization: Bearer $TOK" -H "Content-Type: application/json" \
  -d '{"account":{"ref":"U1"},"product":{"code":"S1-A","subcode":"X1"}}' \
  localhost:8082/s1/subscribe

# Valid S2 subscribe — U2 + a catalog item (expect 200 OK). U1 with the same body
# returns 200 with {"status":"ERROR","reason":"user not eligible"}.
curl -s -H "X-Api-Key: s2-secret-key" -H "Content-Type: application/json" \
  -d '{"user":"U2","item":{"itemId":"S2-100"}}' \
  localhost:8083/s2/subscribe

# S3 metrics — static bearer (NOT an A1 token). One product per call:
curl -s -H "Authorization: Bearer s3-static-token" \
  "localhost:8084/s3/metrics?service=S1&code=S1-A&subcode=X1"
curl -s -H "Authorization: Bearer s3-static-token" \
  "localhost:8084/s3/metrics?service=S2&itemId=S2-100"
```
