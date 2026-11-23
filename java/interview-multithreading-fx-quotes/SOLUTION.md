# Debrief: a consistent FX quote book

Sequence numbers order updates **within a pair**. A publication must keep each quote's sequence, bid and ask together. A two-pair batch must additionally decide and publish both changes against the same book.

## First green test

A simple sequential book can store the last quote and reject a stale update:

```java
Quote current = quotes.get(pair);
if (current != null && incoming.sequence() <= current.sequence()) return false;
quotes.put(pair, incoming);
return true;
```

At sequence 41, writer A checks 42 and pauses. B checks 43 and publishes. A resumes and writes 42. The freshest quote has been lost, even if individual map operations are thread-safe. Keeping bid and ask in separate mutable fields also lets a reader combine a new bid with an old ask.

## Publish values, then publish whole versions

Use immutable quotes:

```java
record Quote(long sequence, long bid, long ask) {
    boolean valid() {
        return sequence > 0 && bid > 0 && bid <= ask;
    }
}
```

For single-pair updates, comparing and replacing *that pair's quote* atomically is sufficient. The loser rereads the current sequence before deciding whether to retry. Invalid inputs cause no state change. A reader returns one quote object rather than assembling fields across reads.

For batches, keep `AtomicReference<Map<String, Quote>>` and publish immutable maps. Validate input, read one book and check **both** pairs against it. If either is stale, return false; otherwise apply both replacements to a private map and CAS the old book to its immutable successor. On a lost race, repeat both sequence checks. Single-pair writers must use the **same** reference. A snapshot is just one read, so readers need no lock and cannot see a partially installed batch. Two separate calls to read one pair each are not a snapshot.

For example, both pairs are at 10. A batch proposes EUR/USD=11 and GBP/USD=11 while a separate writer publishes GBP/USD=12. If 12 wins first, the batch must reject without advancing EUR/USD. If the batch wins first, EUR/USD ends at 11 and GBP/USD can still reach 12. Rolling back EUR/USD after a partial batch would expose a combination that never should have existed.

**Test to keep:** seed both pairs, then submit a batch whose first update is fresh and whose second is stale. Assert the snapshot is *unchanged*, not merely that the call reported failure. In a read/write race collect one snapshot per read and check it against the two legal versions. Copying the book per write is the price of an inexpensive coherent read in this small exercise.
