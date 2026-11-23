# hack-forge

---
Personal project for learning different programming languages and concepts. 

## Java

Requires a locally installed JDK 25.
Run `./java/gradlew -p java assemble` from the repository root.

- [Multithreading refresher](java/multithreading/README.md) — theory and 32 hands-on concurrency exercises.
- [Rate-limiter interview starter](java/interview-rate-limiter/README.md) — unfinished senior Java exercise with opt-in acceptance and test-repair tasks.
- [Money-transfer interview starter](java/interview-money-transfer/README.md) — unsafe, unfinished PostgreSQL ledger exercise; Docker required for tests.

### Build It: 40-minute interview drills

Pick one brief, write a failing test for each new requirement, and open its `SOLUTION.md` after the timer. Each starter uses an in-memory store, Java 25, JUnit and Mockito; the exercises use Java 21-compatible APIs.

| Drill | What to practice |
|---|---|
| [Ticket office](java/interview-multithreading-ticket-office/README.md) | Sell the last tickets without overselling on a retry. |
| [Room booking](java/interview-multithreading-room-booking/README.md) | Move a reservation without losing it if the new dates are taken. |
| [Load balancer](java/interview-multithreading-load-balancer/README.md) | Select a backend while the registry changes. |
| [URL pool](java/interview-multithreading-url-pool/README.md) | Assign the last short code and preserve reverse lookup. |
| [Account ledger](java/interview-multithreading-account-ledger/README.md) | Transfer money atomically and deduplicate retries. |
| [TTL cache](java/interview-multithreading-ttl-cache/README.md) | Share an in-flight load without blocking callers. |
| [Event inbox](java/interview-multithreading-event-inbox/README.md) | Requeue a claim without letting a stale worker acknowledge it. |
| [FX quotes](java/interview-multithreading-fx-quotes/README.md) | Publish quotes in sequence and read consistent snapshots. |
| [Auction](java/interview-multithreading-auction/README.md) | Decide which bids count when the auction closes. |

### Revolut interview resources

- **Official guidance:** [How to ace your Java interview at Revolut](https://www.revolut.com/blog/post/how-to-ace-your-java-interview-at-revolut/) covers live coding, testing and multithreading.
- **Candidate reports — load balancer:** [incremental in-memory registry prompt](https://github.com/pulkitent/revolut-load-balancer#problem-statement) and [Java solution with random and round-robin selection](https://github.com/natalialopesmoreno/live-coding-revolut).
- **Candidate report — URL shortener:** [October 2025 timed, tests-first exercise](https://github.com/DivnychAndrii/url-shortener-interview-oct2025#readme), including a predefined pool of short URLs.
- **Money transfer:** [Glassdoor live-coding question](https://www.glassdoor.com/Interview/Live-coding-exercise-to-implement-money-transfer-between-accounts-focus-on-perfomance-and-concurrency-discussion-on-how-t-QTN_7387323.htm), [candidate's Java implementation](https://github.com/micael-vicente/revolut-live-coding/blob/HEAD/src/main/java/mv/revolut/live/transferapp/BalanceTransferService.java), and [older backend assignment with concurrency review feedback](https://github.com/peshrus/revolut-interview-backend-2019#review-result-with-my-comments).
- **Interview format:** [Glassdoor question on incremental requirements, concurrency, and TDD](https://www.glassdoor.com/Interview/Live-coding-exercise-with-focus-on-perfomance-and-concurrency-and-testing-bonus-for-using-tdd-Three-requirements-you-on-QTN_7387322.htm).
- **Community practice:** [load balancer, URL shortener, and ledger exercises](https://github.com/virtyaluk/revolut_coding) and [Java 25 ledger, token-bucket, and TTL-cache katas](https://github.com/tabmadi/revolut-interview-prep). These are preparation resources, not an official question pool.

## Kotlin

Both modules use stable Kotlin 2.4.21 (language/API 2.4), JDK 25, and JVM 25 bytecode.
With `JAVA_HOME` pointing to JDK 25, run `./kotlin/gradlew -p kotlin build`.

## Rust

- [UUID CLI](rust/uuid/README.md) — generate v4 or v7 UUIDs with an optional count.
