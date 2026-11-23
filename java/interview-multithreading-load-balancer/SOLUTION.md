# Debrief: ten backends

The registry has three promises: at most ten unique addresses, selections from a real membership version, and (in round-robin mode) one turn per call. Registration order matters; random picks must not consume turns.

## Start with a list

Sequentially, a list gives insertion order and simple random access:

```java
boolean register(String address) {
    if (addresses.contains(address) || addresses.size() >= 10) return false;
    return addresses.add(address);
}
```

With nine entries, thread X sees a free slot and pauses. Y sees a free slot and appends. X now appends too. Even if each list method is thread-safe, the compound decision admits eleven. Similarly, a random reader might choose index 1 from a two-element list, then find only one entry by the time it indexes the list. Visibility alone fixes neither race.

## Publish membership as a snapshot

For tasks 2–3, use an atomic reference to an immutable list. Read once, check uniqueness and capacity on that list, append to a private copy, and compare-and-set (CAS) the old reference to the new list. On a failed CAS, repeat **both** checks. Remove the same way, preserving survivor order. An absent removal or rejected registration takes effect at the snapshot read; success takes effect at publication.

For a random pick, keep the *same* local snapshot for both size and indexing:

```java
var members = registry.get(); // AtomicReference<List<String>> for tasks 2–3
if (members.isEmpty()) return Optional.empty();
int index = ThreadLocalRandom.current().nextInt(members.size());
return Optional.of(members.get(index));
```

An overlapping removal might finish before this selection returns. Returning the removed address is still correct: it was registered at the snapshot read. A selection that starts after the removal returns cannot read that pre-removal snapshot.

## Make a turn belong to a membership version

A separate atomic counter can hand out distinct turns for a **fixed** list. It doesn't atomically reset the turn when the list changes. Suppose `[A,B,C]` has just returned `A`, so the next index is 1. A writer removes `A`, publishes `[B,C]`, and pauses before resetting the cursor. A reader now picks `C` at index 1. That's impossible under the contract: before removal the next pick was `B`, and after removal the restarted cycle also begins at `B`. Put both facts in one state:

```java
record State(List<String> members, int next) {
    State { members = List.copyOf(members); }
}

Optional<String> roundRobin() {
    for (;;) {
        var before = state.get(); // AtomicReference<State>
        var members = before.members();
        if (members.isEmpty()) return Optional.empty();
        int index = before.next();
        var after = new State(members, (index + 1) % members.size());
        if (state.compareAndSet(before, after)) {
            return Optional.of(members.get(index));
        }
    }
}
```

Register and remove with that **same reference**, publishing a new membership with cursor zero on each successful edit. Failed edits publish nothing. Random selection reads the members from one state and ignores the cursor. Even when the cursor wraps, each successful turn publishes a fresh state object; a caller holding an older reference cannot accidentally commit a stale turn.

Only the successful CAS consumes the turn. For stable `[A,B,C]`, 30 successful calls consume ten full cycles regardless of thread completion order. A stalled caller cannot stop others: readers keep their immutable snapshot and writers retry instead of waiting for a lock. This does not guarantee fairness to a particular writer. Membership copies stay small because the capacity is ten.

**TDD checkpoint:** Test the exact sequential cycle first. Under concurrency join all 30 calls and assert a histogram, not a sequence based on future completion order. For reset behavior, pick `A` from `[A,B]`, fail to register duplicate `A`, and expect `B` next. Pick `A` again, successfully append `C`, and expect `A` again. Test random selection by membership and single-backend cases rather than demanding a particular random distribution in a short run.
