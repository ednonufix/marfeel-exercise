# Subscription Registry — Exercise

## What you're building

A **Subscription Registry**: a backend service that is the source of truth for
who is subscribed to what. Users subscribe to products that live in downstream
services; the registry coordinates those subscriptions and owns the record of
them. Concretely it:

- exposes a **catalog** of products that can be subscribed to, gathered from the
  downstream services;
- lets a user **subscribe** to one or more of those products in a single request,
  forwarding each subscription to the right downstream service and recording the
  outcome;
- exposes a user's current **subscriptions** and the **history** of their
  subscription attempts;
- lets a user **unsubscribe**.

You integrate against four mock downstream services — **A1**, **S1**, **S2**, **S3** — plus a **MySQL** database, all
provided in [`exercise/`](./exercise). You don't
modify them. Their endpoints, payload shapes and per-user behavior are documented
in [`exercise/README.md`](./exercise/README.md).

---

## Run the dependencies

```bash
cd exercise
docker compose up --build        # A1, S1, S2, S3, and MySQL
# while developing, skip the 60s wait:
# S1_LATENCY_MS=500 docker compose up --build
```

Default ports (all configurable in `exercise/.env`): A1 `8081`, S1 `8082`,
S2 `8083`, S3 `8084`, MySQL `3307` (db/user/pass = `registry`). See
[`exercise/README.md`](./exercise/README.md) for the full contracts and a smoke
test. Your registry runs on the host and talks to these over `localhost`.

---

## The downstream services

```
                          ┌─────────────────────────┐
      U1 / U2 / U3        │  Subscription Registry  │   (the service you build)
    ────────────────────▶ │  Java 21 + Spring Boot  │
                          │  MySQL                  │
                          └──┬────────┬────────┬────┘
                             │        │        │
              ┌──────────────┘        │        └──────────────┐
              │                       │                       │
        ┌─────▼─────┐           ┌─────▼─────┐           ┌──────▼──────┐
        │    S1     │           │    S2     │           │     S3      │
        │  slow     │           │  fast     │           │  metrics    │
        │  bearer   │           │  X-Api-Key│           │  bearer     │
        │  (via A1) │           │           │           │  (static)   │
        └─────▲─────┘           └───────────┘           └─────────────┘
              │
        ┌─────┴─────┐
        │    A1     │  issues the bearer token S1 requires
        └───────────┘
```

- **A1** — issues and validates the token that S1 requires.
- **S1** — a subscription service. Slow, and authenticated with a token obtained
  from A1.
- **S2** — a subscription service. Fast, and authenticated with a static key.
- **S3** — a **product metrics** service. Given a single product identity it
  returns metrics for that product (a subscriber count and an IBAN). Authenticated
  with a static bearer token — which looks like S1's, but is *not* issued by A1.
  It answers one product per call.

S1 and S2 each expose their own catalog of subscribable products and their own
subscribe endpoint, in their own shape; S3 exposes only metrics, keyed by a
product's identity in that service's shape. Exact endpoints, payloads and behavior
are in [`exercise/README.md`](./exercise/README.md). Part of the exercise is
integrating with how each one actually behaves — read the contracts and design
accordingly.

---

## The users

Three seed users — **U1**, **U2**, **U3** — drive the scenarios. They have
different access to the registry, and the downstream services respond differently
depending on the user. See [`exercise/README.md`](./exercise/README.md) for the
per-user downstream behavior.

---

## What we expect

### Endpoints

- **Catalog** — expose the catalog of subscribable products gathered from S1 and
  S2, excluding the ones the user is already subscribed to. **Each product in the
  response must be enriched with its metrics from S3** (subscriber count and IBAN).
  This is the *read-from-downstream* path.
- **Subscribe** — subscribe a user to one or more **specific products** in a
  single request. A subscription is **per product**. This is the *write-to-downstream* path. Subscribing to *all*
  products (and unsubscribing
  from all) may be offered as an optional convenience.
- **Read a user's subscriptions** — the products a user is currently subscribed to (one per product), together with
  their history; also narrowable to a single
  service; and a view of **failed attempts**, including failures for products the
  user doesn't currently hold. **The current subscriptions must also carry each
  product's S3 metrics.** How you shape these reads (one endpoint with filters, or
  several) is your call — we want to see the choice.
- **Get one** subscription.
- **Unsubscribe** from a product.
- **No update endpoint.**

Keep the catalog endpoint (data coming *from* S1/S2) and the subscribe endpoint (data going *back to* S1/S2) clearly
separate.

These are plain JSON HTTP endpoints — there is **no UI to build**. A REST API you
can drive with `curl`/Postman is all that's expected.

### Data model

We expect a data model that lets you store and query:

- which products a user is subscribed to — a subscription is **per product**;
- the history of success / failure for each product subscription attempt.

How you model this is your work to get right.

### Security (bonus)

Authenticating the registry's own users, role checks, and ownership are a **bonus**, not the focus. Simplify the
mechanism as much as you like — an
in-memory user store with HTTP Basic is plenty, or stub it entirely and describe
how you'd do it for real. Spend your time on the core flows first.

---

## Stack — Java + Spring Boot

This is a Java/Spring role, so build the registry on **Java 21 + Spring Boot**.
One downstream service is deliberately slow, and handling that without tying up
threads is central to the exercise.

We grade the *reasoning*, not dogma. If you choose a blocking model for part or
all of it (MVC + virtual threads, or plain servlet), that's acceptable — but be
ready to defend it against the slow-dependency problem. "Reactive isn't worth the
complexity here, and here's why" is a strong answer; reaching for WebFlux by
reflex and not being able to explain it is not.

The database is **MySQL** (provided, ready to go); Postgres is fine if you prefer
it.

---

## Deliverable

- Your registry source plus a short `README` covering: your stack choice and why,
  how to run it against `exercise/`, how to run the tests, and any trade-offs or
  "with more time I'd…" notes.
- Don't modify anything in `exercise/`.

When in doubt, prefer making a trade-off explicit over hiding it.
