# Senior Java interview: in-memory rate limiter

**Runnable, deliberately unfinished starter — not production protection.**
Aim for a 90–120 minute pairing session: prioritize a correct vertical slice,
explain tradeoffs, and list unfinished work. No reference solution is included.
Do not build authentication, Redis integration, plan tiers, or a framework.

## Start from the repository root

Use an installed JDK 25 (adjust this path on your machine). The existing wrapper
is Gradle 9.9.1; this module follows the repository's Spring Boot 3.5.16 conventions.

```sh
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
./java/gradlew -p java :interview-rate-limiter:check :interview-rate-limiter:bootJar
./java/gradlew -p java :interview-rate-limiter:bootRun
```

The app uses port 8080. In another terminal:

```sh
curl http://localhost:8080/health
curl -H 'X-API-Key: demo-client' http://localhost:8080/api/greeting
curl -H 'X-API-Key: demo-client' http://localhost:8080/api/items
curl -H 'X-API-Key: demo-client' -H 'Content-Type: application/json' \
  -d '{"message":"hello"}' http://localhost:8080/api/echo
```

`bootJar` produces `java/interview-rate-limiter/build/libs/interview-rate-limiter-0.0.1-SNAPSHOT.jar`,
also runnable with `$JAVA_HOME/bin/java -jar ...`.

## Delivered vs. your work

Delivered: JSON endpoints, a Boot-registered `OncePerRequestFilter`, a transport-free
`RateLimiter` port and decision/settings records, an **allow-all** in-memory stub,
and injected UTC `Clock`. There is no limiter state, algorithm, eviction, identity
validation, or HTTP enforcement yet. Even requests with missing keys currently
reach the controllers. The filter intentionally does not call the limiter yet.

Boundaries follow the repository's ports/adapters style without extra layers:
`core` owns decisions, time, and eventual state; `adapter/http` owns headers and
servlet responses; `config` composes them. Keep HTTP out of the algorithm. The
constructor/bean seams allow either algorithm and per-test clocks.

## Target contract (not all implemented)

### HTTP

| Request | Successful response |
| --- | --- |
| `GET /health` | `200 {"status":"UP"}` |
| `GET /api/greeting` | `200 {"message":"Hello, candidate!"}` |
| `GET /api/items` | `200 ["book","pen"]` |
| `POST /api/echo` with a JSON string-to-string object | `200` with that object |

* Apply the limiter to `/api` and `/api/...`, on the initial request, before
  controller routing. Other paths are outside its scope. In particular,
  `GET /health` requires no key, never calls the limiter, and consumes no quota.
* Require one `X-API-Key` header. A missing, empty, or whitespace-only value
  returns **400**, JSON `{"error":"missing_api_key"}`, no `Retry-After`, and no
  limiter call. Strip surrounding whitespace with `String.strip()`; keys are
  case-sensitive. Keys identify clients only: no registry or authentication.
  Duplicate headers, key-size limits, and malicious cardinality are discussion
  topics, not hidden requirements.
* Each valid API request attempts exactly one permit. The **same client shares
  quota across all API endpoints and methods**. Distinct keys are independent.
  A permit admitted before routing is consumed even if routing/body handling
  subsequently returns an error.
* On admission, continue the chain once and do not add `Retry-After`.
* On denial, do not run the controller/chain. Return **429**, JSON content type,
  and exactly `{"error":"rate_limit_exceeded"}`. Set `Retry-After` to integer
  delta seconds: round the positive `Decision.retryAfter` **up**, minimum 1.
  Examples: 1 ms → 1, 1000 ms → 1, 1001 ms → 2, 2500 ms → 3.
  Do not expose keys in responses or logs. No extra rate-limit headers required.

### Algorithm, time, and state

Defaults in `LimiterConfiguration`: **capacity 5**, **window 10 seconds**,
**idle TTL 5 minutes**. Tests may use smaller capacities with the same window.
Assume valid settings; if you externalize them, validate capacity > 0, window > 0,
and idle TTL >= window.

Choose **token bucket OR sliding window**, document which, and keep these common
guarantees:

* A new client has a full budget. At one instant exactly `capacity` requests can
  succeed; the next is denied. Denials do not consume permits.
* After a whole window without admissions, a full burst is available, including
  exactly at the boundary. A token bucket may refill continuously; a sliding
  window may expire old admissions. Intermediate refill behavior is deliberately
  unspecified here.
* An allowed decision has zero retry delay. A denial reports a positive duration
  until one permit could next be admitted with no competing traffic (at most a
  window). Keep precise durations in core; round only at the HTTP boundary.
* Use the injected `Clock`, not wall-clock reads or sleeps. Supplied tests advance
  time monotonically. State your policy for clock rollback/large jumps.
* Admission must be atomic per client, including first access. Concurrent requests
  cannot overspend quota; unrelated clients should not need one global lock.
* Implement `cleanup()` using the clock, returning the number of removed clients.
  A client is idle after at least `idleTtl` since its last acquire attempt, allowed
  or denied. Do not evict earlier or reset active quotas. Removing safely expired
  state must preserve future admission behavior. Decide how cleanup is triggered
  and how its work/memory is bounded; a scheduler is not supplied.

## Candidate tasks

1. Implement the chosen per-client algorithm behind `RateLimiter`, with
   Clock-driven decisions and atomic state transitions.
2. Complete the HTTP filter: scope, identity validation/normalization, shared
   quota, chain short-circuiting, and the 429/rounded-retry contract.
3. Implement safe idle cleanup and explain its trigger/lifecycle and races with
   acquisition. Keep implementation small and explain the memory bound.
4. Diagnose the two explicitly flawed tests, repair their assumptions, and add
   missing coverage. Do not change the contract to make defective tests pass.

Acceptance: starter checks stay green; valid acceptance tests and repaired
diagnosis tests pass; add evidence for the chosen algorithm and cleanup. Partial
completion with sound reasoning is preferable to speculative infrastructure.

## Test categories and selection

```sh
# Default runnable-starter checks; also used by repository-wide check
./java/gradlew -p java :interview-rate-limiter:test
./java/gradlew -p java :interview-rate-limiter:check

# Opt-in JUnit 5 @Tag("interview") exercises: EXPECT NONZERO in this starter
./java/gradlew -p java :interview-rate-limiter:interviewTest

# Select a cohesive exercise category (wildcard includes nested classes)
./java/gradlew -p java :interview-rate-limiter:interviewTest --tests '*LimiterAcceptanceTest*'
./java/gradlew -p java :interview-rate-limiter:interviewTest --tests '*HttpAcceptanceTest*'
./java/gradlew -p java :interview-rate-limiter:interviewTest --tests '*IntentionallyFlawedTest*'
```

Reports: `build/reports/tests/test/index.html` and
`build/reports/tests/interviewTest/index.html` inside this module. `check` does
**not** depend on `interviewTest`; no disabled tests or ignored failures hide
exercise status.

Expected untouched-starter results (parameter invocations counted separately):

| Category | Tests | Pass | Fail |
| --- | ---: | ---: | ---: |
| Default starter checks | 5 | 5 | 0 |
| Core acceptance | 7 | 3 | 4 |
| HTTP acceptance | 11 | 2 | 9 |
| Intentionally flawed diagnosis | 2 | 1 | 1 |

Thus `interviewTest` starts with **20 tests, 14 intentional failures**, no skips.
Thirteen failures expose unfinished behavior; one exposes a defective assertion.

* `StarterApplicationTest`: real Boot context/filter registration and MockMvc
  requests, JSON endpoints, default UTC/settings wiring. It asserts runnable
  behavior, **not** completed enforcement.
* `LimiterAcceptanceTest`: valid, algorithm-neutral acceptance at the core port.
  Same-instant overflow, independent clients, whole-window recovery, positive
  retry delay, and bounded concurrent contention. Each scenario owns its clock
  and limiter; executors/futures are bounded and cleaned up without sleeps.
  Some acceptance tests already pass an allow-all stub; that is not proof of a
  correct limiter.
* `HttpAcceptanceTest`: valid focused MockMvc integration with real lightweight
  collaborators and per-scenario clocks. Precise retry rounding and health bypass
  use a fixed-decision port input to isolate HTTP translation, not to simulate
  the algorithm. Missing-key rejection, shared/normalized identity, and denial
  mapping intentionally fail until implemented.
* `IntentionallyFlawedTest`: **both tests are intentionally wrong** diagnosis
  exercises, not acceptance evidence. One assumes a global budget; the other
  assumes an endpoint-specific budget. The first fails even against a correct
  solution; the second happens to pass this stub but must fail a correct solution
  until repaired. A green result here is not necessarily good.

Missing candidate coverage: cleanup removal/counts, no premature eviction,
cleanup/acquire races, denied attempts not extending quota exhaustion, exact
retry-eligibility boundaries, recovery after denial, non-API bypass, missing-key
no-charge, POST quota sharing, and chosen-algorithm intermediate timing/clock
rollback policy. The supplied contention test is useful acceptance, not a proof
of thread safety or exhaustive stress testing. No idle-cleanup behavior is
currently tested or implemented.

## Evaluation / discussion

Review correctness at boundaries, time control, isolation, useful assertions,
and clarity of incomplete work. Explain map `compute` vs. immutable state/atomics,
linearizability, contention, races during eviction, and why a concurrent map alone
is insufficient. Consider fairness, unbounded client cardinality, and whether a
periodic or incremental cleanup strategy meets the chosen operational bound.

Optional discussion only: per-plan capacities/windows and configuration changes;
multi-instance limits with Redis/Lua atomicity, expiration, time authority,
network failure/fail-open vs. fail-closed policy, and consistency tradeoffs.
Do not implement those extensions during the core exercise.
