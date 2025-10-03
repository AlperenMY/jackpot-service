# Jackpot Contribution & Reward Service

Java 17 / Spring Boot 3 backend that manages jackpot pools, contributions, and rewards. Bets are accepted through REST or Kafka, contributions are accumulated in Redis atomically, and rewards are paid when random chance determines a winner. Kafka (KRaft mode) and Redis run alongside the service via Docker Compose.

## Architecture

```
+-----------------------------+
|      REST Controllers       |
|  (/bets, /jackpots/evaluate)|
+-------------+---------------+
              |
              v  publish bet
+-------------+---------------+
|    Kafka Producer (Spring)  |
+-------------+---------------+
              |
              v  jackpot-bets topic
+-------------+---------------+
| Kafka Consumer & Services   |<-- seeded RNG for determinism in tests
| Contribution store + reward |
+-------------+---------------+
              |
              v  atomic Lua-like compare/add
+-------------+---------------+
|      Redis (Pools, Rewards) |
+-----------------------------+
```

## Features

- Fixed and variable contribution strategies driven by `application.yml`.
- Fixed and variable reward chance strategies with bounds and hard limit → 100% chance.
- Atomic jackpot pool changes in Redis, retries on contention, and pool reset after every win.
- Kafka producer + consumer with bet deduplication (`ProcessedBet` records in Redis, TTL 7 days).
- Consumer persists every bet contribution (`betId`, `playerId`, `jackpotId`, stake, contribution, pool snapshot, timestamp) before any reward decision.
- Reward evaluation happens on-demand via REST, using the stored contribution snapshot and current pool state.
- English-only logs, responses, and README.
- Deterministic random number generator for tests (seed configured via properties).
- Bootstrapper seeds three sample jackpots (`Jackpot One`, `Jackpot Two`, `Jackpot Three`) on startup.
- Docker Compose stack for Kafka (KRaft), Redis, Redis Commander UI, and the Spring Boot service.

## Configuration

All tunable values live in [`src/main/resources/application.yml`](src/main/resources/application.yml). Key sections:

- `jackpot.contribution`: fixed rate and variable rate parameters (start, min/max, decay).
- `jackpot.reward-chance`: probability definitions including hard limit.
- `jackpot.random.seed`: RNG seed (override per environment if needed).
- `jackpot.bootstrap`: jackpots preloaded into Redis at startup.
- `jackpot.kafka.topic`: Kafka topic for bet events (default `jackpot-bets`).

Override values using environment variables (`KAFKA_BOOTSTRAP_SERVERS`, `REDIS_HOST`, `REDIS_PORT`, etc.).

## Running the stack

Prerequisites: Docker & Docker Compose.

```bash
docker compose pull
docker compose up --build
```

This builds the Spring Boot jar and starts Kafka (KRaft), Redis, the API (`http://localhost:8080`), and Redis Commander (`http://localhost:8081`). Skip `--build` (and add `-d`) when you just want to restart without rebuilding.

> Need to pin Kafka to a specific version? Override the image tag in `docker-compose.yml` (service `kafka`) with one of the published Bitnami tags, e.g. `bitnami/kafka:3.7.0-debian-12-r2`.

### Manual run (without Docker)

1. Start Kafka & Redis however you prefer.
2. Export the expected environment variables, for example:
   ```bash
   export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
   export REDIS_HOST=localhost
   ```
3. Build & run:
   ```bash
   mvn spring-boot:run
   ```

### Maven setup for local testing

Install Maven 3.9+ if you plan to run unit tests outside Docker. Common options:

- macOS (Homebrew): `brew install maven`
- Ubuntu/Debian: `sudo apt update && sudo apt install maven`
- Windows (winget): `winget install Apache.Maven`

Verify the installation with `mvn -v`. Once Maven is available you can execute `mvn test` as described below.

## Testing

Run the unit tests with:

```bash
mvn test
```

Tests cover contribution strategies, reward chance behaviour, and the contribution→evaluation workflow (reward amount matches current pool, duplicate bets rejected). A seeded RNG ensures deterministic outcomes.

## REST API

### Publish a bet (enqueue via Kafka)

```bash
curl -X POST http://localhost:8080/bets \
  -H 'Content-Type: application/json' \
  -d '{
        "betId": "bet-123",
        "jackpotId": "one",
        "playerId": "player-42",
        "betAmount": 25.50
      }'
```

_Response: 202 Accepted_

```json
{ "status": "Bet accepted for processing" }
```

### Evaluate a bet (after contribution recorded)

```bash
curl -X POST http://localhost:8080/jackpots/evaluate \
  -H 'Content-Type: application/json' \
  -d '{
        "betId": "bet-124"
      }'
```

_Response: 200 OK_

```json
{
  "betId": "bet-124",
  "jackpotId": "one",
  "won": false,
  "rewardAmount": 0
}
```

- Contribution missing (bet not yet consumed) → `404 Not Found`.
- Duplicate `betId` on publish → `409 Conflict` (idempotent processing).
- Missing jackpot → `404 Not Found`.
- Validation errors → `400 Bad Request` with field details.

## Kafka Topic

- Topic name: `jackpot-bets` (override via `jackpot.kafka.topic`).
- Producer publishes each accepted bet.
- Consumer only records contributions and pool updates; rewards are decided by the evaluation endpoint and logged separately.

## Concurrency & Idempotency

- Jackpot pool increments use a Lua-based compare-and-add script executed through `RedisTemplate`. If the stored pool was modified between read and update, the operation retries with the latest value.
- Pool values are stored in cents to avoid floating-point drift. Every update returns the post-contribution pool, ensuring the reward amount equals the pool at the instant of winning.
- Rewards persist before the pool reset. Reset is an atomic Redis write to the initial pool value.
- Idempotency is enforced by `ProcessedBet` entities keyed by `betId` with a TTL of 7 days for contributions, and by re-reading stored contributions/rewards during evaluation.

## Design Notes & Assumptions

- Strategy parameters are configuration-driven so operators can tune probabilities and contributions without code changes.
- Variable contribution decreases linearly based on the current pool (`startRate - currentPool/decayStep`) and is clamped within configured bounds.
- Variable reward chance increases with pool size and reaches 100% once `hardLimit` is met.
- All monetary values use `BigDecimal` and are rounded to cents when stored.
- Logging includes `betId` and `jackpotId` for every publish, consume, contribution, and reward event to aid observability.
- Redis is the single source of truth for jackpot pools; database updates and resets reflect Redis state immediately after atomic operations.
- Tests rely on Mockito stubs for Redis/Kafka integration while verifying business rules.
