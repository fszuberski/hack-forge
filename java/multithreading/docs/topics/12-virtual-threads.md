# 12 · Virtual threads and blocking-service migration

## Theory

Java 21 virtual threads are lightweight JVM-managed threads, scheduled onto carrier
platform threads. Many supported blocking operations unmount a virtual thread,
freeing the carrier. They make thread-per-request blocking code scalable for
I/O-heavy work; they do not speed up CPU computation or increase database capacity.

Use a virtual-thread-per-task executor, not a small reusable virtual-thread pool.
Limit scarce resources with semaphores/connection pools. Millions of queued virtual
threads still retain request data and can exhaust memory; resource limits,
admission control, and request deadlines remain necessary.

**Version matters:** on JDK 21, blocking while holding a synchronized monitor can
pin a carrier; native/foreign calls can also cause pinning. JDK 24's JEP 491 removes
the monitor-related pinning limitation, not every possible source of pinning. Do
not teach "synchronized always pins virtual threads" as a timeless rule. On 21 use
JFR or `-Djdk.tracePinnedThreads=full` to investigate, and choose lock designs from
evidence, not blanket rewrites.

ThreadLocal is supported, but per-thread caches/resources can become very expensive
with task-per-thread execution. Clean request context in finally, don't leak it into
unrelated callbacks, and avoid using it to cache scarce connections per virtual
thread. Structured concurrency and scoped values are outside this Java 21
non-preview course; understand the manual ownership they aim to simplify.

## Exercise A · Bounded virtual fan-out

Implement `virtualthreads/VirtualFanOut.java`; run `*VirtualFanOutTest`.

- Run every callable in its own virtual thread using an owned per-task executor.
- Require positive maxConcurrent; use a semaphore to cap active callable bodies
  independently of the number of virtual threads. Preserve result input order.
- On first observed failure or caller interruption, cancel outstanding futures,
  terminate the executor, and propagate ExecutionException/InterruptedException.
- Assume callbacks cooperate with interruption. Empty input returns an empty list.
- Do not implement the limit using a fixed platform pool or pool virtual threads.

Discuss admission limits if input contains millions of jobs; this exercise accepts
a finite in-memory list and bounds active calls, not total retained request objects.

## Exercise B · Migrate a blocking gateway

Implement `virtualthreads/BlockingGateway.java`; run `*BlockingGatewayTest`.

The legacy design uses a fixed pool of two platform request handlers. The test
models independent blocking requests whose gates require all eight to start.

- Keep the blocking Callable API, but use a separate virtual thread per request.
- Collect strings in input order; don't rewrite this as callbacks/CompletableFuture
  merely to avoid blocking the calling thread.
- Own, close, and terminate the executor on success and failure; cancel siblings
  on failure/interruption. No live owned task may remain after returning normally.
- Empty input succeeds. Propagate original failure via ExecutionException.
- Unlike A, this exercise intentionally has no downstream limit; explain where
  production limits would go and why this artificial barrier needs eight arrivals.

## Hints

<details><summary>First nudge</summary>
Preserve familiar blocking control flow. Change the execution model and resource
limits separately rather than trying to tune a larger platform-thread pool.
</details>

<details><summary>More specific</summary>
Use newVirtualThreadPerTaskExecutor. Put permit acquisition inside each task, with
release only after successful acquisition. Cancel unfinished futures **before**
closing the executor on exceptional paths; close alone waits rather than cancelling.
</details>

## Interview

1. What bottlenecks remain after replacing a platform pool with virtual threads?
2. How does the pinning advice differ between JDK 21 and JDK 24+?
3. Why can a per-thread connection cache become dangerous with virtual threads?
4. Why doesn't Future.cancel(true) guarantee an uncooperative task will terminate?
