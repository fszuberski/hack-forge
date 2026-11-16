# 15 · Deadlock, starvation, and lock contention

## Theory

Classic deadlock needs mutual exclusion, hold-and-wait, no forced preemption,
and circular wait. Consistent global lock ordering breaks the cycle. Timeouts
bound individual waits but can still cause synchronized retries/livelock. A stalled
service may instead have starvation, saturated pools, long lock holds, external
I/O, or a hot CAS loop; not every stall is a monitor deadlock.

Choose lock granularity deliberately. One coarse lock is easy to reason about but
serializes unrelated work. Per-key locks improve independence but can leak lock
objects or split protection if lock entries are removed too early. Fixed stripes
bound memory at the cost of collisions: unrelated keys on the same stripe still
serialize. Multi-key operations need an ordering across stripes as well.

If a callback must run **exactly once**, a CAS retry loop/ConcurrentMap update helper
with repeatable callbacks may not satisfy that contract. Running callbacks under a
lock gives serialization but makes latency, reentrancy, and lock-order risks part
of the API contract. Profile with representative workloads before optimizing.

## Exercise A · Repair lock-order inversion

Implement `contention/DeadlockFreeLedger.java`; run `*DeadlockFreeLedgerTest`.

The broken pseudocode acquires the source lock then the destination lock. Two
transfers A→B and B→A can each hold one lock and wait on the other.

- Construct a positive account count, each with the same nonnegative opening
  balance. Require valid account indexes and positive transfer amounts.
- Use one interruptible explicit lock per account. Always acquire involved locks
  in ascending **account index order**, regardless of transfer direction.
- Atomically transfer or return false for insufficient funds. A self-transfer is
  a successful no-op. Assume long arithmetic does not overflow.
- `balances()` locks all accounts in that same order and returns a detached,
  internally consistent snapshot; release locks in reverse order.
- On interruption release every acquired lock and propagate. Avoid intrinsic-lock
  deadlocks in the test JVM: those waits cannot be cancelled by interruption.

Write the wait-for graph of the broken version, then explain why a global order
precludes a cycle. Do not "solve" it with one global lock or random sleep.

## Exercise B · Remove a coarse-lock bottleneck

Implement `contention/StripedAggregator.java`; run `*StripedAggregatorTest`.

The old design runs every key's update callback under one synchronized method.

- Initialize a positive number of independent stripes. Choose each key's stripe
  with `Math.floorMod(key.hashCode(), stripes)` so negative hashes work too.
- `get` returns zero for absent keys. `update` invokes the supplied operation
  exactly once on the old value and atomically stores/returns its result.
- Same-key operations serialize; keys on different stripes can run concurrently,
  even when one callback is paused. A throwing callback leaves the old value intact.
- Protect storage correctly too: unrelated locks do **not** make one shared HashMap
  safe. Use separate per-stripe maps or a suitable concurrent backing map.
- Callbacks must be short/non-blocking in production and may not recursively
  acquire other keys' stripes. The test deliberately pauses one callback to prove
  independence, not to recommend blocking while holding a lock.

## Hints

<details><summary>First nudge</summary>
List every code path that acquires more than one lock, including read/snapshot paths.
The whole system must agree on one ordering, not just transfer itself.
</details>

<details><summary>More specific</summary>
For A use min/max index and track successful acquisitions for cleanup. For B route
both get and update through the same stripe function; never protect one HashMap
with different unrelated locks. "a" and "b" land on different stripes with 16 stripes.
</details>

## Interview

1. How would you distinguish deadlock from lock contention in two thread dumps?
2. Why can timeout-and-immediate-retry lead to livelock?
3. What consistency does a sum across all stripes need, and how would you provide it?
4. What correctness hazard arises when a per-key lock is removed while waiters exist?
