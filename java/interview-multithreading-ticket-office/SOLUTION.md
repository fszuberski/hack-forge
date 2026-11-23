# Debrief: the last ticket

The office must sell at most its initial stock. A group buys all its tickets or none. By the final task, the office must also remember which quantity and outcome belong to each request ID.

## Why the first green test is not enough

For sequential buyers, a counter does the job:

```java
boolean buyOne() {
    if (remaining == 0) return false;
    remaining--;
    return true;
}
```

Start with one ticket. Alice passes the check and pauses. Bob passes the check, decrements to zero, and returns success. Alice resumes, decrements to minus one and **also** returns success. Another interleaving can lose a decrement instead. Making the field volatile does not make the check and the debit one operation.

## Solve the stock problem first

For tasks 2–3, one atomic integer suffices. Compare-and-set (CAS) means “replace this value only if it is still the value I read.”

```java
boolean buy(int quantity) {
    if (quantity <= 0) throw new IllegalArgumentException("quantity");
    for (;;) {
        int before = remaining.get(); // remaining is an AtomicInteger
        if (quantity > before) return false;
        if (remaining.compareAndSet(before, before - quantity)) return true;
    }
}
```

With five left, both group leaders might calculate `5 - 3`. Only one can publish `2` in place of `5`. The other retries, reads `2`, and declines. The winning CAS is the instant the sale takes effect; the decline takes effect at the read that found insufficient stock. Do not decrement first and refund on failure: other buyers could observe a balance that no completed sale justifies.

## Make retries part of the same decision

An atomic counter plus a separate concurrent receipt map still has a gap: two copies of `r7` can both miss the receipt and both debit stock before either records it. For this small in-memory drill, keep a single atomic reference to an immutable snapshot:

```java
record Receipt(int quantity, boolean accepted) {}
record State(int remaining, Map<String, Receipt> receipts) {
    State { receipts = Map.copyOf(receipts); }
}
```

Initialize with the starting stock and an empty map. For a positive quantity and a supplied ID:

1. Read the current snapshot. If the ID exists, return its stored decision for the same quantity, or a conflict for a different quantity. Neither path changes stock.
2. If it is new, calculate the outcome and next stock. Copy the receipts, add the decision **even on decline**, and build a new snapshot. For a purchase without an ID, use the same state and skip the receipt.
3. CAS the old snapshot to the new one. On success return the decision; on failure start over, including the receipt lookup.

Now even a retry arriving between the winning CAS and the winner's return sees the receipt. Publication is the instant both facts become true. A paused buyer holds no lock; other buyers may finish, though a particular buyer can repeatedly lose a CAS. Copying the receipt map costs time proportional to the number of IDs, a trade-off for making this bounded drill easy to reason about.

**TDD checkpoint:** On a fresh office with five tickets, release two workers to buy three under the same ID. Collect both results with timeouts: both must say success and two tickets must remain. Then reuse the ID for one and assert conflict and unchanged stock. Use a start gate rather than sleeps; concurrent runs exercise the invariant but do not prove every schedule safe.
