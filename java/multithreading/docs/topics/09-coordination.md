# 09 · Latches, barriers, phasers, and semaphores

## Theory

Choose a synchronizer by the protocol, not by familiarity:

| Primitive | Protocol |
|---|---|
| CountDownLatch | One-shot gate; count down to zero, cannot reset |
| CyclicBarrier | Fixed set rendezvous repeatedly, optional barrier action |
| Phaser | Repeated phases with dynamic party registration/deregistration |
| Semaphore | A limited number of concurrent resource users; no thread ownership |

A latch's pre-countDown actions become visible after a successful await; analogous
API-specific ordering guarantees apply to barriers, phasers, and semaphore release/
acquire. Registration is not arrival; forgetting to arrive/deregister can stall a
phase forever. Phaser's default arriveAndAwaitAdvance is **not interruptible**;
use the interruptible advance-wait variant when your cancellation protocol needs it.
On failure, arrange coordinated termination rather than strand remaining parties.

A semaphore permit is not a mutex owned by the acquiring thread. Incorrect release
can inflate capacity. Always release exactly once after successful acquisition,
including when the callback throws; never release if acquisition was interrupted.

## Exercise A · Downstream request limiter

Implement `coordination/RequestLimiter.java`; run `*RequestLimiterTest`.

- Construct with positive permits. `call(action)` acquires one permit interruptibly,
  executes the action on the **calling thread**, and releases in finally.
- Return the result or propagate the action's exception unchanged.
- Never exceed the permit count, leak permits on failure, or invent a permit when
  acquisition is interrupted. Expose availablePermits for diagnostics only.
- No executor or thread creation. This wrapper must work with platform or virtual
  caller threads equally well.

Add a waiting-caller interruption test and verify that capacity remains unchanged.

## Exercise B · Phased inclusive prefix sum

Implement `coordination/PhasedPrefixSum.java`; run `*PhasedPrefixSumTest`.

- Return inclusive prefix sums as longs: `[3,-1,4] → [3,2,6]`; do not mutate input.
- Require positive workers, support empty input and more requested workers than
  elements. Cap actual workers at input length for non-empty inputs.
- Partition into contiguous ranges. In phase 1 compute local prefixes and each
  partition total. In phase 2 publish offsets (sum of earlier partitions). In phase
  3 add the offset to each local prefix. Synchronize phases with a **Phaser**.
- Use long arithmetic before addition; handle uneven partitions.
- Own and join the workers. If the caller is interrupted, terminate the phaser,
  interrupt workers, join them, and propagate interruption. Worker failures must
  not leave other parties stranded; surface them after cleanup.

Explain which phase boundary makes each partition's writes visible. The functional
test cannot verify that you actually used Phaser—review the implementation too.

## Hints

<details><summary>First nudge</summary>
For A, put the try/finally **after** acquisition succeeds. For B write out partition
totals and offsets on paper for three uneven ranges before starting threads.
</details>

<details><summary>More specific</summary>
One designated worker can compute all offsets after the local-sum phase. Everyone
arrives again before adjustments. Use arrive plus awaitAdvanceInterruptibly and a
shared failure path with forceTermination, rather than an uninterruptible barrier wait.
</details>

## Interview

1. When would a latch be insufficient but a phaser appropriate?
2. Why doesn't semaphore fairness imply equal response times?
3. What happens to waiting parties if a barrier participant fails?
4. How do you choose a downstream concurrency limit independent of thread count?
