# 02 · Java Memory Model and safe publication

## Theory

The JMM defines which writes a read may observe, not a literal cache-flushing
algorithm. A data race occurs when conflicting accesses are not ordered by
happens-before and at least one is a write. Correctly synchronized programs enjoy
the sequential-consistency guarantee; reasoning from a single laptop's behavior
is insufficient.

Happens-before is transitive. Unlocking a monitor happens-before a subsequent lock
of that **same** monitor; writing a volatile field happens-before a subsequent read
of that field. A volatile reference can publish all preceding initialization writes,
but does not make the referenced mutable object magically thread-safe.

`final` fields have special initialization guarantees when an object is properly
constructed and `this` does not escape. Final references do not imply deep
immutability. Immutable objects still need a reliable publication mechanism for
replacement and availability. Prefer immutable records plus a volatile reference,
locks, or a concurrent container. Double-checked locking requires a volatile field;
an initialization-on-demand holder is often simpler for a singleton.

## Exercise A · Publish immutable configuration

Implement `memory/PublishedConfig.java`; run `*PublishedConfigTest`.

- `publish(endpoint, maxConnections)` creates a new immutable `Config` snapshot.
  For this exercise any integer value is allowed; endpoint must be non-null.
- Before publication `current()` returns null; afterward it returns one whole
  published snapshot. No reader may combine fields from different versions.
- Multiple writers/readers are allowed. A previous snapshot never changes.
- Use safe reference publication, no locks, and no separate mutable endpoint/count
  fields. State the exact happens-before edge used by a reader.

The concurrent test checks snapshot consistency and basic behavior. It **does not
prove safe publication**: removing volatile may still pass every run. Review the
implementation against the JMM; use jcstress separately for outcome experiments.

## Exercise B · Visibility-safe shutdown signal

Implement `memory/ShutdownSignal.java`; run `*ShutdownSignalTest`.

- Start false; `request()` sets the one-way signal to true, idempotently.
- Any reader may call the non-blocking `isRequested()` without taking a lock.
- Use the weakest simple primitive that provides the required visibility. No
  executor, queue, polling loop, or sleeping belongs in this signal class.
- Explain why the same primitive would **not** make a `requests++` counter safe.

The test's bounded reader loop avoids hanging the test JVM and intentionally has
no writer-to-reader latch after publication. Even so, a broken implementation may
pass due to the runtime/compiler/environment. Provide the correctness argument.
For a real service blocked on I/O or a queue, use interruption/API-specific cancellation
in addition to a flag rather than spending CPU polling.

## Hints

<details><summary>First nudge</summary>
Distinguish visibility of a reference from atomicity of a workflow. Think about
what a reader must observe before dereferencing a newly published object.
</details>

<details><summary>More specific</summary>
A volatile snapshot reference handles A; a volatile boolean handles B. Construct
the snapshot completely before assigning the reference. Reading the reference
once gives a stable local snapshot for subsequent field reads.
</details>

## Interview

1. Why can a program be wrong despite passing a million concurrency tests?
2. Why is `volatile Map<K,V>` not a thread-safe mutable map?
3. What breaks if a constructor registers `this` with a listener before finishing?
4. Why does non-volatile double-checked locking lack a valid publication argument?
