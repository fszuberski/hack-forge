# hack-forge

---
Personal project for learning different programming languages and concepts. 

## Java

Requires a locally installed JDK 25; all seven modules use Java 25 toolchains.
Run `./java/gradlew -p java assemble` from the repository root.

- [Multithreading refresher](java/multithreading/README.md) — theory and 32 hands-on concurrency exercises.
- [Rate-limiter interview starter](java/interview-rate-limiter/README.md) — unfinished senior Java exercise with opt-in acceptance and test-repair tasks.
- [Money-transfer interview starter](java/interview-money-transfer/README.md) — unsafe, unfinished PostgreSQL ledger exercise; Docker required for tests.

## Kotlin

Both modules use stable Kotlin 2.4.21 (language/API 2.4), JDK 25, and JVM 25 bytecode.
With `JAVA_HOME` pointing to JDK 25, run `./kotlin/gradlew -p kotlin build`.

## Rust

- [UUID CLI](rust/uuid/README.md) — generate v4 or v7 UUIDs with an optional count.
