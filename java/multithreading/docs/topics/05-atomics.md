# 05 · Atomics, CAS, and scalable counters

## Theory

Atomic classes expose linearizable operations on individual variables. CAS
(`compareAndSet`) changes a value only if the observed expected value still matches.
A retry loop can implement a compound transition, but may re-run its computation;
side effects inside `updateAndGet` are therefore unsafe. A CAS success is a useful
linearization point. Lock-free system progress does not imply per-thread fairness
or wait-freedom. A class using CAS is not automatically lock-free as a whole.

CAS can miss an A→B→A history (ABA) when only current value/identity matters.
Versioned state or `AtomicStampedReference` may be needed when history matters.
Represent multi-field invariants as a single immutable state held by an atomic
reference, or use a lock; two independent atomics don't form one transaction.

`AtomicLong.get()` is a linearizable read. `LongAdder` spreads updates under
contention; `sum()` is not an atomic snapshot during concurrent updates. It is
good for metrics, not enforcing a hard quota or generating unique sequences.
After writers finish and results are joined, the sum is exact (ignoring overflow).
Neither primitive guarantees better throughput for every workload; use JMH.

## Exercise A · CAS lifecycle state machine

Implement `atomics/LifecycleState.java`; run `*LifecycleStateTest`.

- Start NEW. The only legal edges are NEW→RUNNING→STOPPING→TERMINATED.
- `transition(expected, next)` validates the **edge first**, throwing
  `IllegalArgumentException` for a forbidden edge even if expected is stale.
- For a legal edge, use CAS: return true only if expected matched current state.
  Many simultaneous attempts at the same edge must have exactly one winner.
- Reject null states; no synchronized methods or external locks.
- Explain why this one-way graph has no ABA problem, and how restart support
  would change that reasoning.

## Exercise B · Two metric counters

Implement `atomics/MetricCounters.java`; run `*MetricCountersTest`.

- Maintain two independent counters, one `AtomicLong`, one `LongAdder`, both zero.
- Implement corresponding increments and reads using the appropriate primitives.
- Guarantee exact final counts after all writers finish. Do **not** claim that the
  two readings together are an atomic paired snapshot.
- Write down which implementation you would choose for request metrics, a rate
  limit, and a unique ID generator, with reasons.

As a measurement task, sketch a JMH benchmark varying writers (1/2/8/32), reporting
JDK/hardware and throughput. Do not add timing assertions to the unit test; reads
and resets during updates change the semantics being compared.

## Hints

<details><summary>First nudge</summary>
The state machine needs a conditional operation, not separate get-then-set calls.
The counter APIs deliberately do not return the new scalable counter value.
</details>

<details><summary>More specific</summary>
Validate an edge independently of current state, then call compareAndSet once.
For B use incrementAndGet/get versus increment/sum; don't emulate LongAdder using
one volatile field per thread without considering publication and cache effects.
</details>

## Interview

1. Why can a CAS-loop callback execute multiple times?
2. What does linearizable mean, and why doesn't `LongAdder.sum()` qualify generally?
3. When does ABA matter, even with garbage collection?
4. Why might one highly contended atomic become a cache-coherence bottleneck?
