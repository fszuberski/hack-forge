# 04 · Explicit locks and conditions

## Theory

`ReentrantLock` adds interruptible acquisition, timed `tryLock`, optional fairness,
and multiple `Condition` wait sets. Always release acquired locks in `finally`.
Never unlock a lock you did not acquire. A fair lock reduces barging in particular
acquisition modes; it is not an OS scheduling guarantee and may reduce throughput.

`Condition.await` releases its associated lock and reacquires it before returning.
Like monitor waits, it needs a loop around a state predicate. Separate notEmpty
and notFull conditions avoid waking unrelated waiters. Closing changes predicates
for **both** populations; signal all waiters on both conditions.

For end-to-end timeouts, calculate a deadline with `System.nanoTime()` and pass
the remaining budget to each blocking operation. Don't grant a fresh timeout for
each lock. Wall clocks can jump. `ReadWriteLock` can help read-heavy workloads but
adds overhead; `StampedLock` optimistic reads require validation, is not reentrant,
and is not a drop-in replacement for an invariant guarded by an ordinary lock.

## Exercise A · Timed money transfer

Implement `locks/TimedTransfers.java`; run `*TimedTransfersTest`.

- Transfer a positive amount between two accounts atomically, or return false
  on insufficient funds or lock timeout without changing either balance.
- Acquire locks interruptibly with **one nonnegative timeout budget**, release
  every acquired lock on every path, and propagate `InterruptedException`.
- A self-transfer is a successful no-op. Zero timeout means immediate attempts.
- Both balance changes happen while both accounts are locked; don't debit then
  try to acquire the destination. Assume sums fit in a long for this exercise.
- Different-account opposite-direction transfers must not wait forever. Timed
  failure is acceptable; discuss ordering/retry backoff to mitigate livelock.

The account's package-private lock lets the test hold one side deterministically.
This is a test seam, not a suggested public banking API.

## Exercise B · Closeable queue

Implement `locks/CloseableQueue.java`; run `*CloseableQueueTest`.

- Use a `ReentrantLock`, two conditions, positive capacity, non-null items, FIFO order.
- `put` waits for capacity while open; once closed it throws `IllegalStateException`,
  including producers already waiting. `take` waits for data while open.
- `close()` is idempotent. Existing items remain consumable; a closed, drained
  queue returns `Optional.empty()` immediately. Wake all producers and consumers.
- All blocking acquisition/waits must propagate interruption and preserve invariants.
- Linearize close against put so no insertion occurs after closure wins the lock.

## Hints

<details><summary>First nudge</summary>
Track which locks were actually acquired. Draw a state table for open/closed crossed
with empty/non-empty/full before writing the queue's wait loops.
</details>

<details><summary>More specific</summary>
For A compute remaining nanoseconds before the second tryLock. For B, a producer's
wait predicate includes `!closed`; check closed again after waking. A consumer only
reports end-of-stream when **both** closed and empty.
</details>

## Interview

1. How do safety, deadlock avoidance, and livelock avoidance differ for transfers?
2. Why is `finally { lock.unlock(); }` wrong if acquisition can fail before the try?
3. Why must queue close signal both conditions with signalAll rather than signal?
4. When would a read/write lock be slower than a simple mutex?
