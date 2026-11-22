# Subscription Registry

Java 21, Spring Boot, WebFlux, R2DBC and MySQL. The registry owns subscription state and attempt history. WebClient lets
a slow S1 subscription wait for its response without occupying a request thread. Catalog reads from S1 and S2 can
overlap. Subscription batches process distinct products sequentially. S3 supplies current metrics on reads.

## Run locally

Install JDK 21 and Docker with Compose. From the repository root:

```sh
docker compose --project-directory exercise -f exercise/docker-compose.yml up --build -d
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`. Wait for MySQL to accept connections before starting the application.
Liquibase initializes the schema. The supplied Compose stack reads `exercise/.env` and publishes MySQL at
`localhost:3307/registry` by default. Spring Boot imports `.env` from the repository root. Keep the database settings,
downstream ports and service credentials aligned between the two. Override `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`,
`DB_PASSWORD`, `A1_PORT`, `S1_PORT`, `S2_PORT`, `S3_PORT`, `S2_API_KEY` or `S3_TOKEN` through the environment. For a
shorter demo set `S1_LATENCY_MS=500` in the shell that launches Compose.

The registry listens on port 9000. Use the `API-Version: v1` header and HTTP Basic. The local demo credentials are
`U1 / U1`, `U2 / U2` and `U3 / U3`. Each user can access their own routes. In production use TLS and an identity
provider.

## Routes

All routes below start with `/users/{userId}`.

| Method | Path                                              | Response                                                                                                                                                                      |
|--------|---------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GET    | `/catalog`                                        | Available S1 and S2 products enriched with subscribers and IBAN from S3.                                                                                                      |
| POST   | `/subscriptions`                                  | One result per distinct requested product, in request order.                                                                                                                  |
| GET    | `/subscriptions`                                  | Active subscriptions with metrics and the full attempt history. Optional active filters are `service`, `code`, `subcode`, `itemId`. S1 owns code and subcode. S2 owns itemId. |
| GET    | `/subscription-attempts?status=FAILED&service=S2` | Attempts in reverse chronological order. Both filters are optional. Includes failed products the user has never held.                                                         |
| GET    | `/subscriptions/{id}`                             | An active subscription with metrics, or 404. Use the numeric ID from the active list.                                                                                         |
| DELETE | `/subscriptions/{id}`                             | Remaining active subscriptions with metrics, including an empty array after the last removal. Repeating a deletion returns the current list. History is retained.             |

```sh
curl -u U1:U1 -H 'API-Version: v1' -H 'Content-Type: application/json' \
  -d '{"products":[{"service":"S1","code":"S1-A","subcode":"X1"},{"service":"S2","itemId":"S2-100"}]}' \
  http://localhost:9000/users/U1/subscriptions
```

On a fresh database the supplied mocks return this combination for U1. The provider generates the subscription ID.

```json
{
  "results": [
    {
      "ref": {
        "service": "S1",
        "code": "S1-A",
        "subcode": "X1"
      },
      "status": "SUCCESS",
      "providerSubscriptionId": "<provider ID>"
    },
    {
      "ref": {
        "service": "S2",
        "itemId": "S2-100"
      },
      "status": "FAILED",
      "failureReason": "user not eligible"
    }
  ]
}
```

For valid products U2 gets the opposite outcomes. The supplied mocks accept U3 for both services. S1 receives a cached
bearer token from A1, refreshed with a 30-second margin. S2 uses its API key and S3 uses its own static bearer token.

## Run the tests

With JDK 21 and a Docker daemon accessible to Testcontainers, run `./mvnw clean verify` (Windows:
`mvnw.cmd clean verify`). The suite uses JSON WireMock mappings and a disposable MySQL 8 database. It tests the HTTP
contracts, mixed batches, failure history, duplicate requests after a successful subscription, ArchUnit dependency
rules, ownership, deletion and resubscription. The local exercise stack can be stopped during these tests.

## Consistency and trade-offs

Within a batch, duplicate products are processed once. A later request for an active product returns`ALREADY_SUBSCRIBED`
and skips the provider call. A successful subscription and its attempt are persisted together. A rejection records a
failed attempt. Deletion removes the active row and permits resubscription.

Two simultaneous requests can both reach the provider before either active row is committed. The database enforces a
unique active row per user and product. Provider-side idempotency or reconciliation would be needed to handle ambiguous
outcomes after a network or database failure.

The registry reads metrics per product. An S3 failure can prevent delivery of the enriched response, including after a
committed deletion. Retrying the read or the idempotent deletion retrieves the current list. At higher volumes, paginate
attempt history and tune per-provider concurrency against measured latency.
