# 06 · Concurrent collections and atomic per-key workflows

## Theory

`ConcurrentHashMap` supports concurrent retrieval and per-key atomic operations:
`putIfAbsent`, conditional remove/replace, `compute`, and `merge`. A thread-safe map
does not make `if (get(k) > 0) put(k, get(k)-1)` atomic. Put the invariant inside
one supported atomic operation. Keep remapping functions short, non-blocking, and
avoid recursive map modifications or I/O inside them.

Iterators are weakly consistent, not global point-in-time snapshots. Reading size
or iterating while updates run is observational, not a transaction. Storing a
mutable `ArrayList` in a concurrent map doesn't protect mutations of that list.
`ConcurrentHashMap<K, LongAdder>` is useful for frequency maps, but totals across
keys are not an atomic snapshot.

Use `CopyOnWriteArrayList` for rarely modified listener/configuration lists with
snapshot iteration, not write-heavy queues. `BlockingQueue` gives bounded handoff
and interruptible waits; `ConcurrentLinkedQueue` is non-blocking but unbounded
and supplies no blocking take or intrinsic backpressure. Choose based on semantics,
not the word "concurrent" in a class name.

## Exercise A · Frequency tracker

Implement `collections/FrequencyTracker.java`; run `*FrequencyTrackerTest`.

- Concurrent callers record lists of non-null words, incrementing every occurrence.
- Use a concurrent map and safe per-key counters. Empty lists are no-ops.
- `snapshot()` returns an immutable detached map of plain long values, not live
  mutable counters or a view into internal storage.
- Counts are exact after writers stop; while they run, explicitly document a
  weakly consistent observational snapshot, not a global atomic snapshot.
- Discuss how removing/replacing counter objects during writes would complicate
  the algorithm. This exercise only adds counts; it has no removal operation.

## Exercise B · Inventory without overselling

Implement `collections/AtomicInventory.java`; run `*AtomicInventoryTest`.

- Defensively copy non-null initial stock into concurrent storage; negative stock
  is invalid. Caller mutations must not alter inventory.
- `reserve(sku, quantity)` requires positive quantity and returns true iff that
  much stock could be atomically deducted from that SKU. Never go negative.
- Unknown SKUs have zero stock; a rejected reservation changes nothing.
- Implement each reservation as one atomic per-key map workflow; no global lock.
- This is not a multi-SKU transaction. Explain what an all-or-nothing shopping
  cart reservation would additionally require.

## Hints

<details><summary>First nudge</summary>
Map safety and value-object safety are separate questions. Ask what another
thread can do between your check and update.
</details>

<details><summary>More specific</summary>
For A create the counter atomically and increment it; convert counters into longs
for a detached snapshot. For B use compute to decide and decrement together,
recording that invocation's result without sharing a mutable result flag globally.
</details>

## Interview

1. Why isn't `containsKey` followed by `put` an atomic initialization?
2. When is CopyOnWriteArrayList a good trade-off? What do iterators observe?
3. What can `size()` tell you while a concurrent map is changing?
4. Why should a compute callback avoid calling a remote service?
