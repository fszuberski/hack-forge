# Debrief: one room, two guests

Think of a stay as the half-open interval `[in, out)`. Checkout day is available. Two valid stays overlap exactly when **each starts before the other ends**. This also catches a short stay wholly inside a longer one.

```java
record Stay(LocalDate in, LocalDate out) {
    boolean overlaps(Stay other) {
        return in.isBefore(other.out) && other.in.isBefore(out);
    }
}
```

## Why a map of bookings is not enough

A plain map from booking ID to stay is sufficient for sequential calls with fresh IDs:

```java
boolean book(String id, Stay wanted) {
    if (!wanted.in().isBefore(wanted.out())) return false;
    if (bookings.values().stream().anyMatch(wanted::overlaps)) return false;
    bookings.put(id, wanted);
    return true;
}
```

Alice checks an empty calendar for June 10–13. Before she inserts her stay, Bob checks for June 12–15 and sees the same empty calendar. Both checks pass; both guests get confirmations for June 12. Even a concurrent map with atomic insertion **per booking ID** cannot resolve this: Alice and Bob have different keys, but the no-overlap rule covers the whole room.

## Publish one calendar version

Keep one atomic reference to an immutable map of active bookings for the room. Start empty. Values are immutable too. For each operation, decide against one version and publish a revised copy only if that version is still current. For example, after validating the dates, the booking loop is:

```java
for (;;) {
    var before = calendar.get();
    if (before.values().stream().anyMatch(wanted::overlaps)) return false;
    var after = new HashMap<>(before);
    after.put(id, wanted);
    if (calendar.compareAndSet(before, Map.copyOf(after))) return true;
}
```

When two guests race, one publishes. The loser rechecks the winning calendar: it rejects an overlap but still accepts adjacent dates. Input failures have no effect; a conflict is decided at the read of the conflicting version. A successful change takes effect at the compare-and-set (CAS).

| Operation | Decision on the version just read | Change to publish as one version |
|---|---|---|
| Cancel | If the ID is absent, return false. | Remove that ID. If publication fails, reread before claiming success. |
| Reschedule | Require an active ID and valid dates. Check overlaps against **all other IDs**, excluding the guest's own old stay. | Replace that ID's stay in the copy. If publication fails, recheck existence and every conflict. |

For cancellation, compare identity by booking ID, not dates: June 1–3 may already belong to the next guest. For moving, “cancel then book, restore on failure” is unsafe. A third guest could take the old dates in the gap, making restoration impossible. A single replacement leaves the original stay visible until the move actually wins. A concurrent cancellation cannot resurrect a stay: if cancellation wins, the retried move sees an absent ID.

A lock around the whole calendar would serialize decisions correctly, but a paused lock owner would stall the room. With snapshot CAS, a paused caller owns nothing; failed CAS means another update made progress. One caller can still starve. Scanning and copying active stays takes linear time, reasonable for a single room in this exercise.

**TDD checkpoint:** Race the move and fresh booking for June 8–10 from the brief. Collect the two outcomes first. Then probe June 1–3 with a fresh ID: it must be bookable **exactly when the move won**. This checks the rollback promise through the public API, not just the number of successful calls.
