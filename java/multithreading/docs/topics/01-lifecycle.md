# 01 · Thread lifecycle and interruption

## Theory

A `Thread` moves through NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED.
RUNNABLE includes threads eligible for CPU time, not just currently executing.
BLOCKED specifically means waiting to enter an intrinsic monitor; a thread waiting
for a `ReentrantLock` usually parks in WAITING, not BLOCKED.

Calling `run()` directly is an ordinary method call. `start()` starts a new execution
and may be invoked only once. `join()` waits for termination and makes the joined
thread's writes visible to the caller. `sleep()` releases no monitors, establishes
no synchronization edge, and is not a coordination mechanism.

Interruption is a cooperative request, not a kill switch. Blocking interruptible
operations usually throw `InterruptedException` and clear the flag.
`Thread.interrupted()` reads **and clears** the current thread's flag;
`isInterrupted()` does not clear it. Never use deprecated `stop`, `suspend`, or
`resume`. Daemon threads do not keep the JVM alive; that is not a cleanup strategy.

## Exercise A · Cancellable worker

Implement `lifecycle/CancellableWorker.java`; run `*CancellableWorkerTest`.

- Own one **platform thread**, repeatedly invoking the supplied interruptible task.
- `start()` may succeed exactly once. A second call throws `IllegalStateException`.
- `stop()` requests interruption, is idempotent, and does not wait. Calling it before
  `start()` is a no-op; it must not prevent a later first start.
- Exit if interrupted, if the task throws `InterruptedException`, or if the task
  fails with an unchecked exception. Do not silently restart a failed task.
- `awaitStopped(timeout)` joins for at most the supplied nonnegative duration and
  returns whether the worker has terminated; before start return true. Zero means
  **inspect only**, not `join(0)` (which waits forever).
- Synchronize lifecycle transitions so concurrent start/stop calls are safe. The
  task must cooperate with interruption; no class can forcibly stop arbitrary code.

Add tests for stop-before-start, concurrent start attempts, and task failure. Explain
how callers would observe/report unchecked task failures in a production API.

## Exercise B · Coordinated start and completion

Implement `lifecycle/CoordinatedStart.java`; run `*CoordinatedStartTest`.

- Validate `workers > 0`; create exactly that many platform workers with indexes
  `0..workers-1`, each invoking the callback once.
- Use a ready gate and a start gate: no callback begins until **all workers have
  reached readiness**. The calling thread opens the start gate.
- Join every worker before returning. Capture worker failures; after cleanup throw
  `IllegalStateException` with one failure as its cause, rather than losing it on
  an uncaught-exception handler. This includes assertion failures in callbacks.
- On caller interruption, interrupt and join the owned workers, then propagate
  `InterruptedException`. Assume callbacks respond to interruption.
- Do not use executors yet; learn thread ownership explicitly.

## Hints

<details><summary>First nudge</summary>
Separate requesting cancellation from observing completion. Keep a reference to
the owned thread; lifecycle state must itself be safely published.
</details>

<details><summary>More specific</summary>
For B, workers count down a ready latch, await a separate start latch, and then run
their callback. The caller waits for ready, opens start, and joins. Ensure a worker
failure cannot leave the caller waiting on a completion gate forever.
</details>

## Interview

1. What visibility guarantee does successful `join()` provide? Does `sleep()`?
2. Why is setting a boolean insufficient to cancel a thread blocked in `take()`?
3. At which boundary should an interrupt be propagated versus restored?
4. What is the observable difference between BLOCKED and WAITING?
