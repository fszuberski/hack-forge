# Senior Java interview: money transfers

**Runnable, deliberately naive starter. Unsafe: never use with real money.**
This is an interview exercise, not a ledger implementation or reference solution.
Prioritize a small correct slice, explain tradeoffs, and identify unfinished work.
Do not add authentication, messaging, distributed workflows, or an FX engine.

## Build and run from the repository root

Requires JDK 25, the existing Gradle 9.9.1 wrapper, and reachable Docker.
Spring Boot is 3.5.16 with dependency-management 1.1.6.
Tests use real disposable PostgreSQL 17 containers, including during root `check`;
there is no H2, missing-Docker skip, disabled test, or ignored failure.

```sh
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
./java/gradlew -p java :interview-money-transfer:check :interview-money-transfer:bootJar
```

Local demo (Bash; generates disposable credentials **at runtime**, no `.env`):

```bash
export TRANSFER_DB_USER="$(id -un)"
export TRANSFER_DB_PASSWORD="$(openssl rand -hex 24)"
export TRANSFER_DB_URL=jdbc:postgresql://localhost:55432/money_demo
export TRANSFER_DEMO_CONTAINER="money-interview-${USER}-$$"

docker run --rm -d --name "$TRANSFER_DEMO_CONTAINER" \
  -p 127.0.0.1:55432:5432 \
  -e POSTGRES_DB=money_demo \
  -e POSTGRES_USER="$TRANSFER_DB_USER" -e POSTGRES_PASSWORD="$TRANSFER_DB_PASSWORD" \
  postgres:17-alpine

# Bounded readiness wait; the last check must succeed before continuing.
for attempt in $(seq 1 30); do
  docker exec "$TRANSFER_DEMO_CONTAINER" pg_isready -U "$TRANSFER_DB_USER" -d money_demo && break
  sleep 1
done
docker exec "$TRANSFER_DEMO_CONTAINER" pg_isready -U "$TRANSFER_DB_USER" -d money_demo

# Explicit schema/seed setup, only on this disposable database.
docker exec -i "$TRANSFER_DEMO_CONTAINER" psql -v ON_ERROR_STOP=1 -U "$TRANSFER_DB_USER" -d money_demo \
  < java/interview-money-transfer/src/main/resources/schema.sql
docker exec -i "$TRANSFER_DEMO_CONTAINER" psql -v ON_ERROR_STOP=1 -U "$TRANSFER_DB_USER" -d money_demo \
  < java/interview-money-transfer/demo.sql

./java/gradlew -p java :interview-money-transfer:bootRun
# Or: "$JAVA_HOME/bin/java" -jar java/interview-money-transfer/build/libs/interview-money-transfer-0.0.1-SNAPSHOT.jar
```

App port: 8080. In another terminal:

```sh
curl -i http://localhost:8080/transfers \
  -H 'Content-Type: application/json' -H 'Idempotency-Key: demo-transfer-1' \
  -d '{"sourceAccountId":"00000000-0000-0000-0000-000000000001","targetAccountId":"00000000-0000-0000-0000-000000000002","amount":10.00,"currency":"USD"}'
```

Demo accounts: `...001` USD 100.00, `...002` USD 25.00, `...003` EUR 50.00.
Seed inserts do not reset existing balances. **Replaying the curl currently moves
money again.** After Ctrl-C, in the original terminal, inspect committed state:

```sh
docker exec "$TRANSFER_DEMO_CONTAINER" psql -U "$TRANSFER_DB_USER" -d money_demo \
  -c 'TABLE accounts' -c 'TABLE transfers' -c 'TABLE ledger_entries'
# Stop only the demo container created above; --rm removes its disposable data.
docker stop "$TRANSFER_DEMO_CONTAINER"
unset TRANSFER_DB_PASSWORD
```

The app initializes missing tables and validates JPA mappings on startup; it does
not create demo accounts automatically. This simple schema initializer is not a
production migration system.

## Delivered versus unfinished

Delivered: persisted JPA `Account` and `Transaction` (table `transfers`), decimal
balances/currencies, JSON `POST /transfers`, basic structural input validation,
404 mapping, and a naive sequence of debit, credit, and receipt saves.
**Each repository save commits independently.** A missing target can therefore
leave the source debited. The optional key is stored as plain metadata, with no
lookup or uniqueness. `ledger_entries` is an empty SQL seam, never written by the
starter. No overdraft check, business currency/self-transfer check, locking,
optimistic version, transfer-wide transaction, or idempotency is implemented.

Following the existing small ports/adapters style: `core` contains immutable
commands/balance snapshots/receipts and the incoming use case/outgoing store ports;
HTTP and JPA adapters translate at the boundaries; `config` composes the service.
Core has no HTTP, Spring, or JPA dependency. The deliberately sequential store
API is a starting point, not a requirement to preserve it when adding atomicity.

## Target contract — candidate work, not current guarantees

### HTTP and decimal money

`POST /transfers` requires JSON with non-null UUID `sourceAccountId` and
`targetAccountId`, decimal `amount`, and `currency`, plus one `Idempotency-Key`.
Amounts may be JSON numbers or decimal strings (no float/double conversions).
Successful first requests and replays both return **200**, with this shape:

```json
{
  "transactionId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "sourceAccountId": "00000000-0000-0000-0000-000000000001",
  "targetAccountId": "00000000-0000-0000-0000-000000000002",
  "amount": 10.00,
  "currency": "USD",
  "sourceBalance": 90.00,
  "targetBalance": 35.00
}
```

* `amount` must be positive, with incoming `BigDecimal.scale() <= 2`; integers,
  `10.0`, and `10.00` are equivalent money. Even redundant third decimal places
  are rejected. **Never round** accepted or rejected inputs. USD and EUR only,
  exact uppercase, two minor-unit decimal places; no currency normalization.
  Numeric JSON response fields represent exact decimal money; lexical scale
  differences alone are not a contract failure. Balances must remain nonnegative.
  PostgreSQL uses unbounded `numeric`, not silently rounding `numeric(...,2)`.
* Both accounts must exist, be distinct, have the same currency, and match the
  requested currency. There is no conversion, fee, or self-transfer operation.
* Strip surrounding key whitespace with `String.strip()`; normalized length
  1–200 characters, case-sensitive. Identity is **global across all transfers
  in this database**, not scoped to account, payload, process, or HTTP connection.
  Retain successful identities indefinitely for this exercise; no expiry or auth.
  Duplicate headers and hostile input-size limits are discussion topics.
* Require the key even on the first request. After structural validation, an
  existing successful key with the same payload returns the **original receipt**
  (same transaction ID and original balance snapshots), without any extra
  balance, transaction, or ledger mutation. Payload identity includes both
  account IDs, numerically compared amount, and exact currency. Do not compare
  amounts using scale-sensitive `BigDecimal.equals`.
* A successful key reused with a different structurally valid payload is **409**,
  even if that new payload would fail business validation. Concurrent same-key
  callers must converge on one operation/original response, not multiple debits.
  Rejected/rolled-back operations leave no key reservation in this exercise.
  New keys represent independent operations, even for identical payloads.

| Condition | Status |
| --- | --- |
| Malformed/missing fields, invalid UUID/money/currency, missing/blank/oversized key | 400 |
| Self-transfer or account/request currency mismatch | 400 |
| Either account missing | 404 |
| Insufficient funds, including competing concurrent debits | 409 |
| Successful key reused with different payload | 409 |
| Unexpected internal/database failure | 500, with no partial committed effects |

For new identities, validate structure/key first, then account existence, then
self/currency rules, then funds. For existing identities, apply replay/conflict
rules after structural validation, before rechecking changed account balances.
Only status codes are prescribed for errors; an optional JSON error body must
not expose internals. No separate pending/retry/status workflow is required.

### Ledger and concurrency

One successful transfer must atomically persist its receipt, source debit,
target credit, and **exactly two** ledger entries: source `-amount`, target
`+amount`, same transaction/currency, sum zero, each with its post-operation
`balance_after`. The receipt snapshots must agree with those entries. Snapshots
are historical: a later transfer does not rewrite old receipts or entries.
Rejected requests and failed operations leave **no partial effects**.

Under concurrency, no overdrafts, lost updates, duplicate same-key operations,
or inconsistent balance/history snapshots. A->B and B->A must both finish with
consistent balances when funds suffice, without deadlocks escaping as 500s or
hanging the caller. No particular locking or optimistic implementation is mandated.

## Candidate tasks

1. Complete validation and an atomic transfer with balanced postings and consistent
   snapshots/history. Demonstrate rollback after a failure, not just happy paths.
2. Implement database-backed idempotency, replay and payload conflict semantics,
   including concurrent first use. Explain the lookup-versus-unique-constraint race.
3. Protect funds and concurrent balances, and make opposing transfers safe. Choose
   and justify isolation/locking or optimistic conflict handling and lock ordering.
4. Repair the explicitly flawed tests, add missing evidence, and document remaining
   limitations. Do not change the contract just to satisfy a defective assertion.

Review design, not incidental implementation: transaction boundaries, isolation
levels, lock ordering versus optimistic retries, decimal scale-aware comparisons,
database uniqueness versus check-then-insert, useful errors, and test observability.
Do not add a distributed transaction framework or a hidden algorithm requirement.

## Tests: green starter versus opt-in exercises

```sh
# Green runnable-starter checks, also included by root check (Docker required)
./java/gradlew -p java :interview-money-transfer:test
./java/gradlew -p java :interview-money-transfer:check

# EXPECT NONZERO: unfinished acceptance plus explicitly flawed diagnosis tests
./java/gradlew -p java :interview-money-transfer:interviewTest
./java/gradlew -p java :interview-money-transfer:interviewTest --tests '*TransferAcceptanceTest*'
./java/gradlew -p java :interview-money-transfer:interviewTest --tests '*ConcurrencyAcceptanceTest*'
./java/gradlew -p java :interview-money-transfer:interviewTest --tests '*IntentionallyFlawedTest*'
```

Reports in this module: `build/reports/tests/{test,interviewTest}/index.html`;
machine results: `build/test-results/{test,interviewTest}/TEST-*.xml`.
`check` does **not** depend on `interviewTest`.

* **StarterApplicationTest: 5 passing checks**. Real Boot/MockMvc/JPA/PostgreSQL:
  USD/EUR happy paths and three basic invalid-money rejections. These establish
  runnable wiring and committed observable effects, not ledger safety.
* **TransferAcceptanceTest: 15 valid scenarios**, mixed passing/failing. Missing
  source rejection and distinct case-sensitive keys already pass. Empty ledger,
  missing-target partial debit, business rejection, missing keys, replay and
  changed-payload scenarios expose absent guarantees deterministically.
* **ConcurrencyAcceptanceTest: 3 valid bounded scenarios**. Two independently
  executing request actors start at a gate; no test-managed transaction or shared
  persistence context, sleeps, repository mocks, or injected production locks.
  Same-key requests currently persist two receipts regardless of lost-update
  scheduling; competing debits both incorrectly succeed. Opposing transfers can
  pass or fail depending on scheduling: one passing interleaving proves neither
  isolation nor deadlock safety. Database lock/statement timeouts, future deadlines,
  cancellation, and executor termination bound execution; harness/infra errors are
  **not** intentional acceptance failures.
* **IntentionallyFlawedTest: 2 diagnosis exercises, neither is acceptance evidence**.
  Scale-sensitive equality fails for numerically equal balances. The same-key
  double-debit assertion passes this unsafe starter and should fail a correct
  implementation until the assertion is repaired. Green can be false confidence.

All scenarios own unique committed accounts and key namespaces; no global reset
or deletion. A JVM-scoped disposable PostgreSQL container uses library default
test credentials and normal Testcontainers/Ryuk cleanup on worker shutdown.
The module uses Boot-managed Testcontainers 1.21.4 (no version override): its
[release notes](https://github.com/testcontainers/testcontainers-java/releases/tag/1.21.4)
explicitly restore compatibility with recent Docker Engine changes (Docker 29).
No host daemon settings or API-security overrides are needed.

**Candidate-owned missing coverage:** failure after a database write with rollback,
complete validation matrix (including key length/null fields/malformed JSON),
global key conflicts across source accounts, concurrent same-key/different-payload,
later-history versus original replay snapshots, repeated/adversarial interleavings,
and entry invariants under every concurrent scenario. Supplied tests are compact
examples, not proof of concurrency correctness; do not rely on a fixed total
failure count because opposing-transfer outcomes are schedule-dependent.

Followups for discussion only: multi-currency FX and explicit rounding/residual
policies; audit/reconciliation queries comparing current balances with **opening
balances plus transfer postings**. Demo opening balances are not transfer entries:
do not assume the transfer-only ledger must sum to an account's current balance.
Explain how opening entries, immutable history, and reconciliation would evolve.
