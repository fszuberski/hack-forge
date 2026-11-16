# 03 · Monitors, synchronized, and guarded blocks

## Theory

Every object has an intrinsic monitor. `synchronized` provides mutual exclusion,
reentrancy, and visibility through unlock/lock edges. All accesses participating
in a multi-field invariant must use the same locking protocol; synchronizing only
writers does not make unsynchronized readers safe. Lock a private, stable object,
not a replaceable reference, interned string, or publicly accessible object.

`wait()` atomically releases the monitor and suspends, then reacquires the monitor
before returning or throwing. You must own that monitor to call `wait`, `notify`,
or `notifyAll`. Guard waits with **while**, not if: spurious wakeups are allowed,
and another awakened thread may consume the condition first. Notification is
not stored as a permit; state predicates are the source of truth.

`notifyAll()` is generally safer when producers and consumers share one wait set.
Update state and signal while holding the lock. Keep critical sections small;
avoid callbacks, network calls, and long blocking work under a monitor. Intrinsic
monitor acquisition itself is not interruptible and offers no timed try-lock API.

## Exercise A · Repair a counter

Implement `monitors/SynchronizedCounter.java`; run `*SynchronizedCounterTest`.

- Begin at zero, increment by one, and return a `long` value.
- Use intrinsic locking, not atomic classes. Both reads and increments participate
  in the same locking protocol; no updates may be lost under contention.
- Explain why `volatile long count; count++;` would still be broken, and identify
  the linearization point of increment and value.

## Exercise B · Bounded FIFO buffer

Implement `monitors/BoundedBuffer.java`; run `*BoundedBufferTest`.

- Use a private FIFO collection, positive capacity, and monitor guarded blocks.
- `put` blocks while full; `take` blocks while empty. No polling or busy waiting.
- Reject null elements. Support multiple producers/consumers and preserve FIFO
  order as defined by successful insertion linearization, not caller arrival time.
- Propagate `InterruptedException` without corrupting the buffer or losing capacity.
- Use `wait`/`notifyAll`, **not** an existing blocking queue or explicit lock.

Add a multi-producer/multi-consumer test that verifies the exact multiset delivered,
and a cancellation test for a blocked producer. Don't assert fairness unless your
implementation actually provides it.

## Hints

<details><summary>First nudge</summary>
Write predicates first: consumer may proceed iff not empty; producer iff size is
less than capacity. Keep predicate checks and state changes under one monitor.
</details>

<details><summary>More specific</summary>
After waking, re-check the predicate in a loop. After insertion/removal, notify all
potentially eligible waiters. A notification alone never means a waiter may proceed.
</details>

## Interview

1. Why can using `notify()` for both producers and consumers lose liveness?
2. Does a sleeping thread release its monitors? Does a waiting one?
3. What is a spurious wakeup versus a logically obsolete notification?
4. When is reentrancy useful, and when can calling external code under a lock hurt?
