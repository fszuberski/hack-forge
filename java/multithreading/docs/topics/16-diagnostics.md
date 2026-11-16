# 16 · Deterministic tests, thread dumps, and diagnosis

## Theory

Test concurrent behavior with controlled interleavings and observable invariants.
A latch/barrier can place workers on either side of a problematic read/write.
Repeated stress adds coverage but is neither deterministic reproduction nor proof.
Tests can inadvertently repair a visibility bug by adding a happens-before edge;
be explicit about which synchronization is fixture coordination versus the code
under investigation. Use bounded waits, capture worker failures, and always clean up.

Thread dumps are snapshots, not histories. Correlate thread names, states, lock
identities, owners, and stack frames; compare multiple dumps. BLOCKED suggests
intrinsic monitor acquisition, WAITING can mean healthy idleness or missing signals,
and RUNNABLE can include some native I/O, not just productive CPU work. A complete
cycle of ownership/waits suggests deadlock, not simply many waiters on one owner.

`ThreadMXBean` exposes platform-thread state and some ownable-synchronizer/deadlock
diagnostics; it is **not a complete virtual-thread inventory**. For virtual-thread
analysis on JDK 21, investigate `jcmd <pid> Thread.dump_to_file -format=json <file>`
and JFR rather than assuming Thread.print/ThreadMXBean lists every virtual thread.
Use JFR for lock contention/pinning/CPU evidence, JMH for microbenchmarking, and
jcstress for allowed memory-model outcomes. None substitutes for an invariant proof.

## Exercise A · Reproduce a lost update on demand

Implement `diagnostics/LostUpdateLab.java`; run `*LostUpdateLabTest`.

- Start two owned platform workers that each perform one deliberately non-atomic
  increment of a shared counter initially zero.
- Split increment into read and write. Gate execution so **both reads happen
  before either write**. Then allow both writes and join both workers.
- Return Result(observed=1, expected=2), reliably on every run. Do not hard-code
  the result, sleep, or loop until a race happens by chance.
- The counter's read/write synchronization is deliberately wrong for an increment;
  the test-only gates make this interleaving controlled. State that distinction.
- Caller interruption must cancel/join workers rather than leak them. Capture
  worker failures so a failed gate cannot silently return a plausible result.

After observing the failure, locally try a synchronized increment and an atomic
increment separately, updating your own tests to expect two. Keep the supplied lab's
contract as a reproducible failure example; don't commit a "fix" that hard-codes one.

## Exercise B · Classify a real thread snapshot

Implement `diagnostics/ThreadDiagnostics.java`; run `*ThreadDiagnosticsTest`.

- Accept ThreadInfo snapshots; ignore null entries for terminated threads.
- Return an immutable detached Report: names of BLOCKED threads; names of WAITING
  or TIMED_WAITING threads; and a map from each thread name to its known lock-owner
  name, when getLockOwnerName() is non-null. Preserve snapshot order in lists.
- Assume unique thread names for this report API (explain why thread IDs would be
  a stronger production identity). Never infer deadlock from BLOCKED alone.
- Don't acquire application locks or wait for application threads while inspecting
  the already-captured data. Do not mutate the supplied array or JVM state.

Then run the supplied bounded **diagnosticDemo**, without needing either solution:

```sh
# From java/, with JDK 21:
./gradlew :multithreading:diagnosticDemo
# In a second terminal, use the demo's printed PID:
jcmd <pid> Thread.print -l
jcmd <pid> Thread.print -l
```

Find `blocked-inventory-request` and `slow-inventory-owner`, match the monitor
identity, explain why the owner is waiting while still holding that monitor, and
explain why this is **contention, not a deadlock cycle**. It releases within 90 seconds.
As a further check record a 30-second JFR (command in README) and identify which
events corroborate the dump. Do not leave the demo running while timing tests.

## Hints

<details><summary>First nudge</summary>
For A, write the desired trace: T1 reads 0, T2 reads 0, then both write 1.
For B, distinguish a thread's state from its owner's state.
</details>

<details><summary>More specific</summary>
A reusable barrier or two latches can separate the read and write phases. Count
readiness after storing each local copy. For B switch on getThreadState, record
getLockOwnerName when present, and wrap collections with immutable copies.
</details>

## Interview

1. How can a test latch accidentally hide a missing volatile write/read?
2. What evidence distinguishes idle workers, deadlock, and pool starvation?
3. Why might ThreadMXBean-based diagnostics miss your virtual-thread requests?
4. What would you collect before changing thread-pool sizes during an incident?
