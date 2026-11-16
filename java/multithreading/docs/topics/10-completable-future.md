# 10 · CompletableFuture composition and timeout races

## Theory

Use `thenApply` to transform a value, `thenCompose` to flatten an asynchronous
dependency, and `thenCombine` to combine independent results. `allOf` returns a
completion signal, not typed results; `anyOf` returns the first completion, which
may be exceptional. Combining stages does not inherently provide fail-fast
cancellation of sibling work.

Non-async continuations may run on the completing thread or the attaching caller
when already complete. Async variants use the specified executor, otherwise normally
the common pool; don't put blocking service calls there by accident. `get` exposes
checked interruption/ExecutionException; `join` uses unchecked CompletionException
and isn't an interruptible waiting API.

`handle` transforms success or failure, `exceptionally` recovers failure, and
`whenComplete` observes completion (but an observer throwing can affect the derived
stage). Be explicit whether fallback hides failures or only handles timeouts.
`orTimeout`/`completeOnTimeout` operate on the future on which they are invoked;
don't mutate a caller-owned shared source inadvertently. A timeout on the result
does not stop underlying work. CompletableFuture cancellation does not interrupt
the supplier in the way an executor FutureTask may.

## Exercise A · Dashboard aggregation

Implement `composition/ServiceAggregator.java`; run `*ServiceAggregatorTest`.

- Given profile and balance futures, immediately return a future for `Dashboard`.
- Complete successfully when both inputs succeed, regardless of completion order.
- Preserve exceptional completion and original root causes. Do not substitute
  default data or change either input future.
- No get/join, blocking, extra executor, or thread creation. Fail-fast completion
  while the other input is pending is **not** required by this contract.

## Exercise B · Timeout-only fallback

Implement `composition/TimeoutFallback.java`; run `*TimeoutFallbackTest`.

- Return a new independent future racing source completion against a nonnegative
  timeout scheduled on the supplied, **caller-owned** scheduler.
- If source wins, propagate its value or failure. On timeout only, invoke fallback
  once and use its value; a throwing fallback completes the result exceptionally.
- Never complete/cancel the source, including if a caller cancels the wrapper.
  Document that the source may keep running after the timeout.
- Cancel the scheduled timer whenever it is no longer needed, including wrapper
  cancellation. Never shut down the scheduler. Do not block a thread waiting.
- Zero permits immediate timeout. When source/timer race, either may win, but a
  fallback must not run if source completion already won. Use atomic winner
  selection, not a racy isDone-then-call sequence.

Add tests for cancellation, throwing fallback, simultaneous completion, and scheduler
rejection. On scheduler rejection complete exceptionally rather than return a future
that could wait forever. Source continuation attachment and timer-handle publication
can themselves race; review cleanup of already-completed sources carefully.

## Hints

<details><summary>First nudge</summary>
A needs a combinator, not a new task that waits for two tasks. B needs a winner
protocol before performing fallback's potentially visible side effect.
</details>

<details><summary>More specific</summary>
Use a separate atomic decision to choose source versus timer before invoking
fallback. Wire cleanup so a timer created after an early source completion is
cancelled too. Cancelling a wrapper should participate in the same winner protocol.
</details>

## Interview

1. What thread runs a thenApply callback on an already-completed future?
2. Why is thenApply returning a future different from thenCompose?
3. Does a timeout imply a downstream payment attempt never happened?
4. How could fallback unintentionally hide a serious application defect?
