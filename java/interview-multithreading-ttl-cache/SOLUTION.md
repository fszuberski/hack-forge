# Debrief: one load per key

A caller wants a fresh value, not another call to the loader. Distinguish three states for a key: absent, ready until a deadline, or loading on behalf of callers. A loading entry has no expiry deadline; time spent fetching does not consume its TTL.

## Why a miss followed by a load is racy

```java
var hit = get(key);
if (hit.isPresent()) return CompletableFuture.completedFuture(hit.get());
return loader.apply(key).thenApply(value -> {
    put(key, value);
    return value;
});
```

If A and B miss while the first load is unfinished, both invoke the loader. An explicit put of “new” during A's load can also be overwritten by A's late “old” result. Putting `join()` inside a lock hides the duplicate but makes progress depend on another caller's I/O.

## Reserve a place for the result before starting work

One non-blocking design for a small cache is an atomic reference to an immutable map of entries. Use a **unique loading marker** containing a private `CompletableFuture` for each attempt. Compare-and-set (CAS) publishes a revised map only if the version you started from is still current.

- **Read/put:** A ready entry is fresh exactly while `now < expiresAt`. On plain read treat expired entries as misses; eager removal is unnecessary. A put publishes a new ready entry with `clock.instant().plus(ttl)` and replaces any loading marker.
- **Elect a loader:** On a fresh hit return a completed stage. On a loading marker return a read-only view of its promise (e.g. `minimalCompletionStage()`). Otherwise CAS a new marker into this slot. Only the winner invokes the async loader, **outside** the retry loop. Losers reread. Never call the loader from a retriable update function.
- **Complete successfully:** If the same marker still owns the slot, replace it with a ready entry using the current time for its deadline. Retry CAS on unrelated map changes; stop publishing if this key's marker changed. Then complete the old promise for its attached callers, even if an explicit put won the slot.
- **Fail:** Handle a synchronous throw just like an exceptionally completed stage. Remove **only the matching marker**, retrying on unrelated changes, *before* completing its promise exceptionally. A caller that learns of the failure can now retry without rejoining a dead request.

The marker CAS selects the one loader; value publication determines which value a new caller sees. No cache operation waits for I/O or owns a service lock. A stalled loader delays the result for its subscribers, not their ability to obtain a stage or another key's ability to load. The map copy costs time proportional to key count; that's a deliberate simplification for the drill.

**Test to keep:** have the loader return a manually controlled future. Make two calls for `A` while it's incomplete; both must return stages and the invocation count must be one. Complete a load for `B` before releasing `A`. Put a replacement for `A`, then fail the old future: both old subscribers see the error, while a new read gets the replacement. Use an adjustable clock for TTL boundaries rather than sleeping.
