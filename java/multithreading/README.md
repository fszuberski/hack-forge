# Java multithreading refresher

A hands-on course for senior Java interviews: **16 topics, 32 exercises**, Java 21,
JUnit 5, no preview APIs. Budget roughly 1–3 hours per topic, longer for the pipelines
and single-flight implementation. Work in order initially; all exercises are independent
and do not depend on solutions from earlier topics.

## Getting started

Run from the repository's `java/` directory. The existing Gradle 8.5 wrapper needs a
compatible runtime: **use JDK 21**, even if your system defaults to a newer JDK.
The module also requests a Java 21 toolchain; install JDK 21 locally (no automatic JDK
download is configured). For example, on Linux:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :multithreading:test
./gradlew :multithreading:exerciseTest --tests '*CancellableWorkerTest'
./gradlew :multithreading:exerciseTest --tests '*.lifecycle.*'
./gradlew :multithreading:exerciseTest
```

The last command runs **all unfinished exercises and is expected to fail**.
Starter methods throw `UnsupportedOperationException` on purpose. `test`/`check`
compile all starter/test code and check course integrity and test helpers,
**not exercise completion**.
Exercise tests are tagged `exercise`; `exerciseTest` opts into them. In an IDE, run
the individual test class directly. Re-run a test even when Gradle considers it
up-to-date with `--rerun-tasks`. Reports are in `build/reports/tests/<task>/index.html`.

Serial execution is the convenient default. Opt into **two concurrent JUnit workers**
(one Gradle test JVM, fixed pool capped at two) to check fixture isolation:

```sh
./gradlew :multithreading:check -PcourseParallel=true --rerun-tasks
./gradlew :multithreading:exerciseTest -PcourseParallel=true --rerun-tasks
```

Class/package `--tests` filters shown above include nested and parameterized cases.
The parallel option does not change tags or turn unfinished exercises into default checks.

## How to study

1. Read the topic's theory, then explain its guarantees aloud without looking.
2. Read the exercise contract and tests. Implement the corresponding production
   class in `src/main/java/com/fszuberski/multithreading/<package>/`.
3. Run its acceptance test. Add your own adversarial tests: failure, cancellation,
   simultaneous calls, empty input, saturation, and resource cleanup.
4. Use the hints only when stuck. There are no completed reference solutions in
   the starter tree.
5. Write a short correctness argument: shared state, invariants, happens-before
   edges, linearization points, ownership, and shutdown behavior.
6. Answer the interview questions and discuss an alternative design.

Acceptance tests are **a minimum contract, not a proof of thread safety**. Some
requirements (choice of primitive, bounded queues, safe publication, no busy-waiting,
and absence of thread leaks on every path) also require code review. In particular,
no ordinary JUnit test can reliably force a Java Memory Model visibility violation.

## Curriculum

Each link contains theory, two exercise contracts, progressive hints, and interview prompts.
The class names below select the matching `<ClassName>Test`.

| # | Topic | Package | Exercise A / Exercise B |
|---|---|---|---|
| 01 | [Thread lifecycle and interruption](docs/topics/01-lifecycle.md) | `lifecycle` | `CancellableWorker` / `CoordinatedStart` |
| 02 | [Java Memory Model](docs/topics/02-memory-model.md) | `memory` | `PublishedConfig` / `ShutdownSignal` |
| 03 | [Monitors and intrinsic locking](docs/topics/03-monitors.md) | `monitors` | `SynchronizedCounter` / `BoundedBuffer` |
| 04 | [Explicit locks and conditions](docs/topics/04-locks.md) | `locks` | `TimedTransfers` / `CloseableQueue` |
| 05 | [Atomics and CAS](docs/topics/05-atomics.md) | `atomics` | `LifecycleState` / `MetricCounters` |
| 06 | [Concurrent collections](docs/topics/06-collections.md) | `collections` | `FrequencyTracker` / `AtomicInventory` |
| 07 | [Executors and futures](docs/topics/07-executors.md) | `executors` | `ParallelMapper` / `FutureBatch` |
| 08 | [Cancellation and ownership](docs/topics/08-cancellation.md) | `cancellation` | `InterruptibleProcessor` / `GracefulShutdown` |
| 09 | [Coordination primitives](docs/topics/09-coordination.md) | `coordination` | `RequestLimiter` / `PhasedPrefixSum` |
| 10 | [CompletableFuture composition](docs/topics/10-completable-future.md) | `composition` | `ServiceAggregator` / `TimeoutFallback` |
| 11 | [Fork/join and parallel streams](docs/topics/11-parallelism.md) | `parallelism` | `ParallelSum` / `ParallelGrouping` |
| 12 | [Virtual threads](docs/topics/12-virtual-threads.md) | `virtualthreads` | `VirtualFanOut` / `BlockingGateway` |
| 13 | [Pipelines and backpressure](docs/topics/13-pipelines.md) | `pipelines` | `ProcessingPipeline` / `BatchingProcessor` |
| 14 | [Cache and single-flight patterns](docs/topics/14-caching.md) | `caching` | `SingleFlight` / `ExpiringCache` |
| 15 | [Deadlock and contention](docs/topics/15-contention.md) | `contention` | `DeadlockFreeLedger` / `StripedAggregator` |
| 16 | [Testing and diagnostics](docs/topics/16-diagnostics.md) | `diagnostics` | `LostUpdateLab` / `ThreadDiagnostics` |

## Concurrency cheat sheet

- **Concurrency ≠ parallelism:** overlapping work versus simultaneous execution.
  More threads do not necessarily increase throughput; identify CPU, I/O, and lock limits.
- **Safety / liveness:** nothing bad happens / useful work eventually happens.
  Identify starvation and deadlock separately from incorrect results.
- **Visibility / atomicity / ordering:** different properties. `volatile` does not make
  `count++` atomic. A thread-safe container does not make a multi-step workflow atomic.
- **Happens-before:** program order; monitor unlock → subsequent lock of the same
  monitor; volatile write → subsequent read; `start` → started thread; termination →
  successful `join`; task submission → task → successful `Future.get`. The API docs
  specify additional edges for queues, latches, and other synchronizers.
- **Immutability and confinement first:** minimize shared mutable state. Publish
  immutable snapshots safely; don't leak `this` from constructors.
- **Choose the abstraction:** monitor/lock for invariants, atomic for a single state
  transition, concurrent collection for per-key operations, queue for handoff,
  executor for ownership, future for results, semaphore for resource limits.
- **Cancellation is cooperative:** propagate interruption or restore the flag when
  handling it at a boundary; clean up in `finally`. `Future.cancel(true)` is a request,
  not proof the computation stopped. `CompletableFuture.cancel(true)` does not
  interrupt its supplier automatically.
- **Bound everything:** queue size, active downstream calls, waits, retries, and
  shutdown. A timeout should normally be one end-to-end budget, not reset per stage.
- **Virtual threads improve blocking-task scalability**, not CPU speed or database
  capacity. Avoid pools of virtual threads; limit the scarce resource instead.
- **Measure deliberately:** thread dumps/JFR for diagnosis, JMH for microbenchmarks,
  jcstress for memory-model experiments. Repeated green JUnit runs are not a proof.

## Testing conventions and safety

Test names follow `should<ExpectedOutcome>Given<Precondition>`. Related behaviors
use JUnit 5 `@Nested` groups (for example starting/stopping, cancellation, timer
cleanup, and input validation). Small classes covering one cohesive contract remain
flat: adding a wrapper around, for example, counter increments, snapshot publication,
shutdown signaling, or queue closing would only repeat the class's purpose. Separate
setups/assertions for success, failure, and interruption stay distinct rather than
being forced into parameter tables. Equivalent inputs use parameterized tests;
independent repeatability experiments use `@RepeatedTest`, not a case loop that
stops on its first failure. Same-instance grouping repetitions and consecutive
single-flight failures deliberately remain sequential: fresh per-invocation fixtures
would lose their reuse/state-transition coverage. Loops driving concurrent work or
checking all returned results are likewise not alternative test-case discovery.

Fixtures use real queues, executors, futures, synchronizers, and per-test clocks.
Each invocation owns its mutable state; repeated literal keys are local to that
instance, never a shared service. Thread names are unique, and opposing transfers
use logical worker indices rather than incidental JVM thread-ID parity. Scheduling
gates and arithmetic inputs are intentionally deterministic; randomizing schedules
would not improve the contract. No internal collaborators are mocked.

`support/ExerciseContracts` explicitly registers the 32 exercise classes. Integrity
checks compare this registry with discovered exercise annotations and inspect actual
test-method metadata, including nested, parameterized, and repeated tests, for
opt-in tags and positive deadlines. They do not count every `*Test.java` as an
exercise or search source text for annotations. Untagged helper tests can therefore
grow independently and run in the default green suite.

Tests use controlled gates and bounded waits rather than sleeps to guess scheduling.
The shared test helper owns daemon platform threads, cancels work, and verifies
termination and actual worker exit within a bounded cleanup budget, even if its
owner is interrupted. Condition polling parks briefly and respects interruption.
A timeout reports a failure; it must not be used to hide a leak. Negative
blocking checks use a short `Future.get` timeout only after a worker has entered the
operation; they are observations, not formal scheduling guarantees.

A green default run proves curriculum/contract metadata and exercised helper
behavior, not exercise correctness. While starters are TODOs, red serial/parallel
exercise runs can establish scenario discovery, filtering, and cleanup on the paths
actually reached. Compare failure causes as well as counts: parameterization and
split input/failure scenarios intentionally expose cases previously hidden behind
an earlier failing assertion. Such runs cannot validate every unreached gate,
production cleanup path, race, or memory-model guarantee. Re-run both modes after
implementing each exercise and review the code's correctness argument.

Do not remove timeouts or weaken assertions to get green tests. Do not benchmark in
JUnit using elapsed-time thresholds. Do not create an uninterruptible deadlock in the
test JVM. The diagnostics demo is isolated, releases its monitor after at most 90
seconds, and can be inspected independently:

```sh
./gradlew :multithreading:diagnosticDemo
# In another terminal, use the PID printed by the demo, NOT the Gradle daemon PID:
jcmd <pid> Thread.print -l
jcmd <pid> JFR.start name=course settings=profile duration=30s filename=/tmp/opencode/course.jfr
```

The demo's timed wait is deliberate: it supplies an observation window, not test
synchronization. See topic 16 for the diagnostic checklist and limitations concerning
virtual threads.

## Primary references

- [Java 21 concurrency package](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/package-summary.html)
- [JLS §17: Threads and Locks](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)
- [Virtual threads, JEP 444](https://openjdk.org/jeps/444)
- [Monitor pinning change in JDK 24, JEP 491](https://openjdk.org/jeps/491)
- [JMH](https://github.com/openjdk/jmh) and [jcstress](https://github.com/openjdk/jcstress)
