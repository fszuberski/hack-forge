# 07 · Executors, futures, and bounded submission

## Theory

Executors separate task submission from thread creation/lifecycle. Size CPU-bound
pools near available CPU capacity (measure); blocking workloads need a different
budget, often virtual threads with downstream limits. `newFixedThreadPool` limits
workers but uses an **unbounded queue**. `newCachedThreadPool` may create too many
platform threads. Explicit queue capacities and rejection policies are part of
overload design, not afterthoughts.

`Future.get` waits for a result, establishing visibility of task actions. Failure
is wrapped in `ExecutionException`; cancellation produces `CancellationException`.
`submit` captures exceptions in the future, so ignoring the future loses failure
visibility. `execute` has no future result channel. `ExecutorCompletionService`
collects results in completion order rather than submission order.

Never block every worker waiting for child work queued to the same saturated
pool: thread-starvation deadlock can occur without a monitor cycle. Document who
owns an executor. `shutdown` rejects new tasks but lets accepted tasks finish;
`shutdownNow` attempts interruption and returns tasks that never started.
`ExecutorService.close()` (available since Java 19) waits for termination; it is not
a bounded shutdown policy.

## Exercise A · Bounded parallel mapper

Implement `executors/ParallelMapper.java`; run `*ParallelMapperTest`.

- Map inputs concurrently using an owned platform-thread executor; preserve input
  order in results. Require positive parallelism; empty input returns an empty list.
- Allow at most `parallelism` submitted-but-not-collected tasks at any time, not
  merely that many active threads with an unbounded backlog.
- Keep workers busy as results complete. Do not wait for a whole fixed batch when
  one completed task could free an in-flight slot.
- On first **observed** failure, cancel outstanding tasks and throw
  `ExecutionException` with the original cause; on interruption cancel and propagate.
- Shut down and terminate the owned executor on every exit. Assume callbacks
  cooperate with interruption. Don't shut down any global/shared pool.

Add an out-of-order completion test and a failure test with an interruptible sibling.
The tests can observe active parallelism; bounded submission also needs code review.

## Exercise B · Collect futures under one deadline

Implement `executors/FutureBatch.java`; run `*FutureBatchTest`.

- Collect already-submitted futures in their input order. Own their cancellation,
  **not** the executor that produced them. Empty input succeeds immediately.
- Use one nonnegative timeout budget for the entire operation. A completed future
  can be read even with zero remaining budget; a pending one must time out.
- On failure, timeout, cancellation, or interruption cancel every unfinished future
  with `cancel(true)`, then propagate the appropriate exception.
- Return results only when all succeed; don't return partial success silently.

Explain the limitation: ordered collection might not observe a later failed future
until an earlier slow one completes or times out. A fail-fast API needs a different
completion strategy.

## Hints

<details><summary>First nudge</summary>
Separate result ordering from completion ordering. Attach an input index to each
task so completion order need not dictate output order.
</details>

<details><summary>More specific</summary>
For A submit an initial window, take a completion, store by index, and refill one
slot. For B calculate remaining time with nanoTime for every get and cancel in an
exception path. Clamp negative remaining time to zero so completed futures still work.
</details>

## Interview

1. Why can a fixed thread pool still exhaust heap under overload?
2. What rejection policy would you choose, and when can CallerRunsPolicy be harmful?
3. Why is Future cancellation not evidence that a remote side effect was undone?
4. How can nested future waits deadlock an otherwise lock-free-looking application?
