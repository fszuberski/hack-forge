# Debrief: the last short code

Shortening is a reservation: one URL owns one code, that code resolves back to the URL, and the pool can no longer offer it. Returning an existing mapping must still work when the pool is empty.

## First green test

Validate the input before consuming anything. Parse with `URI`, require an HTTP or HTTPS scheme and a host, and keep the original string as the key. Two maps make sequential shortening and resolving easy:

```java
String existing = byUrl.get(url);
if (existing != null) return existing;
if (byUrl.size() >= 100 || free.isEmpty()) throw new PoolExhausted();
String code = free.removeFirst();
byUrl.put(url, code);
byCode.put(code, url);
return code;
```

With one free code `x`, two callers shortening the same new URL both miss in `byUrl`. The first removes `x` and pauses. The second sees an empty pool and fails, even though both calls must succeed. With more codes, they could instead consume two for the same URL. Thread-safe maps alone would also leave a window between publishing the forward and reverse mappings.

## Commit the reservation once

Since there are at most 100 mappings, copy-on-write is affordable. Keep the ordered pool immutable and use one `AtomicReference` to a state containing the next pool index and both immutable maps.

1. Read one state. Return the existing mapping **before** testing exhaustion.
2. For a new URL, check capacity and index bounds. Select the code at the current index; do not remove it from a shared queue.
3. Build private map copies with the new association. Compare-and-set (CAS) the old state to one containing both maps and the incremented index. On failure, discard the copies and repeat the lookup.

The winning CAS is the instant the code is assigned. A losing request for the *same* URL now finds it; a loser for a *different* URL sees exhaustion. Resolve uses one state read, so if a caller starts after assignment returns, both directions already exist. A failed CAS costs a copy but means another operation made progress; no suspended writer holds a lock.

**Test to keep:** use a pool containing only `x`. Release two callers from a start gate, collect both outcomes with bounded waits, then resolve `x`. Test both the identical-URL and different-URL cases. Merely asserting the map size is one would miss an erroneous failure returned to an identical caller.
