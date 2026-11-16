# 14 · Single-flight loading and expiring caches

## Theory

Single-flight deduplicates **concurrent** loads for a key; caching retains results
for later calls. They solve different problems and can be composed. Without
deduplication, an expired hot key can cause a cache stampede. A future placeholder
in a concurrent map gives other callers a result to share before the expensive
loader begins. Never perform blocking remote I/O inside a map's compute callback.

Failure must evict the in-flight entry so a later call can retry. Conditional
`remove(key, sameFlight)` prevents an old completion from deleting a newer flight.
Already-completed loader stages can execute completion callbacks synchronously,
including while the starting method is still wiring its bookkeeping.

Don't expose shared mutable completion/cancellation state to unrelated callers:
one cancelled client's dependent future shouldn't poison all clients' load.
Timeouts on individual callers should normally not mutate the shared source.

For TTL caches store value and expiry in one immutable entry, replace atomically,
and remove stale entries conditionally. Lazy expiry does not proactively bound
memory for never-read keys. Production caches usually also need capacity/eviction,
load timeouts, metrics, negative-caching policy, and a considered time source.
Clock is injectable and testable, but wall-clock jumps affect TTL semantics;
monotonic time is often preferable for process-local elapsed TTLs.

## Exercise A · Single-flight loader

Implement `caching/SingleFlight.java`; run `*SingleFlightTest`.

- For each key, run at most one loader while its returned future is pending.
  Different keys are independent. The loader must return promptly with a future;
  do not add your own executor or block waiting for the result.
- Publish a placeholder before invoking loader, but invoke loader outside map
  remapping functions. Concurrent callers share outcome via independent dependent
  futures. Cancelling one returned future must not cancel the source or others.
- Convert synchronous loader exceptions to exceptional completion, not a throw
  from execute. Reject null key/loader; treat a null loader future as load failure.
- Evict on success, failure, or source cancellation. This is **not a value cache**;
  once completion/cleanup finishes, the next call starts a new load.
- Clean up before publishing completion to callers so a retry after observing
  completion can reliably start fresh. Avoid recursive-load cycles for the same
  key; detect or document unsupported recursion rather than blocking on yourself.

## Exercise B · Expiring cache

Implement `caching/ExpiringCache.java`; run `*ExpiringCacheTest`.

- Require a positive TTL and non-null Clock; reject null keys/values.
- `put` atomically installs a new immutable value/expiry entry using clock.instant().
- `get` returns Optional.empty for absent or expired entries. Expired means
  `now >= expiresAt`, including the exact boundary. Replacing refreshes the TTL.
- If you lazily remove expiry, remove only the entry observed, never a newer value
  installed after the read. Concurrent get/put must not corrupt state.
- Use the supplied clock, not sleep or hard-coded system time. No background thread,
  loader, capacity eviction, or global transaction is required by this exercise.

Add an adversarial expiry/replacement race test using controllable gates and a
controllable clock. State whether a read overlapping a put may return the older entry.

## Hints

<details><summary>First nudge</summary>
Model a flight and a cached entry as different lifetimes. For A, think about the
order: install placeholder → initiate load → cleanup → publish shared outcome.
</details>

<details><summary>More specific</summary>
putIfAbsent can choose the load owner. Return a dependent stage, not the shared
placeholder itself. Remove by key-and-placeholder identity. For B store expiry
together with value and use conditional removal if a read discovers staleness.
</details>

## Interview

1. Why isn't computeIfAbsent with a synchronous remote call an ideal loader?
2. What happens when a caller cancels a future returned to several clients?
3. Why can lazy TTL expiry still leak memory from a production perspective?
4. How would you combine stale-while-revalidate, single-flight, and error backoff?
