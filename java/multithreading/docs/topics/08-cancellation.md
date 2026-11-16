# 08 · Cancellation, interruption, and ownership

## Theory

Cancellation must be designed across every blocking boundary. Interruptible APIs
include queue take/put, latch await, sleep, join, and explicit interruptible locks.
Some I/O APIs have separate close/cancel/timeout semantics; thread interruption
does not magically abort every external operation or reverse completed effects.

When a method can throw `InterruptedException`, propagate it after cleanup. When
handling at a boundary that cannot throw it, normally restore with
`Thread.currentThread().interrupt()` and return/abort, rather than log-and-continue.
An InterruptedException generally arrives with the flag cleared; restoring and
continuing an interruptible loop often creates a failure spin.

Ownership answers who may shut a resource down and who awaits completion. A library
given a shared executor must not close it without an explicit ownership contract.
Use graceful shutdown first, then forced interruption within a separate bounded
budget. Always account for never-started tasks, outstanding futures, open resources,
and work that ignores cancellation. `finally`/try-with-resources help but do not
make an unbounded `close()` automatically suitable for every shutdown deadline.

## Exercise A · Interruptible queue consumer

Implement `cancellation/InterruptibleProcessor.java`; run `*InterruptibleProcessorTest`.

- Repeatedly `take()` a string from the caller-owned queue and invoke the handler.
- Propagate interruption promptly; an already interrupted caller must process
  no item, even if the queue is non-empty. Do not swallow or wrap the exception.
- Handler runtime exceptions propagate; do not retry an item implicitly.
- Don't create threads, clear interruption merely to continue, close the queue,
  or shut down resources owned by the caller.
- Assume a handler cooperates with cancellation or returns promptly. Discuss
  delivery semantics if cancellation occurs after dequeue but before processing.

## Exercise B · Two-phase executor shutdown

Implement `cancellation/GracefulShutdown.java`; run `*GracefulShutdownTest`.

- The caller explicitly grants shutdown ownership of the supplied executor.
- Call shutdown, await up to nonnegative `grace`; if still running, call
  shutdownNow and await up to nonnegative `forcedWait`.
- Return true iff terminated; false if the final budget expires. Don't claim you
  killed uncooperative work. Zero durations mean immediate checks/escalation.
- If the caller is interrupted while awaiting, invoke shutdownNow and propagate
  `InterruptedException`. No need to wait through another full budget on that path.
- Validate durations before changing executor state.

Add a test for caller interruption during graceful waiting. Use explicit gates and
always clean up the fixture in finally, even if an assertion fails.

## Hints

<details><summary>First nudge</summary>
Write a resource-ownership table. For A, the worker loop belongs to the caller;
for B, the shutdown method has permission to terminate the executor.
</details>

<details><summary>More specific</summary>
Let queue.take throw directly; check interruption between iterations as necessary.
For B implement orderly await followed by forced await, with a catch that cancels
before rethrowing. Do not use a catch-and-log block that reports success afterward.
</details>

## Interview

1. When is restoring the interrupt flag appropriate, and when is propagation enough?
2. What is the difference between "future cancelled", "thread interrupted", and "task stopped"?
3. How would you cancel a remote HTTP call while preserving business idempotency?
4. What would you report if forced shutdown timed out during deployment?
