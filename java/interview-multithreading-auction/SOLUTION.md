# Debrief: the closing bid

A bid decision concerns a single auction at a particular instant. Its result is historical: an accepted bidder can subsequently be outbid. Closing freezes the leader. By the final stage, the auction must also remember what it said to each bid ID.

## First green test

The natural sequential implementation checks the rules and replaces the leader:

```java
if (closed || amount <= price) return false;
price = amount;
leader = bidder;
return true;
```

At 100, both 110 and 120 pass the check. If 120 writes first and 110 writes last, the final price falls to 110. A second failure needs only one bidder: the 120 call passes the check, close declares the old winner at 100, then the bidder writes 120 and reports success. There is no possible ordering that justifies both results.

## Make the auction one decision

Start with one atomic reference to an immutable state holding the open/closed flag and the leading bid (absent initially). A bid reads the state, checks that it is open and strictly improves the price, then uses compare-and-set (CAS) to publish a new open state with that bidder and amount together. On a failed CAS, repeat the checks. A lower bid may succeed *before* a higher one but can never overwrite it.

Close uses the same CAS loop to publish the observed leader with `closed=true`. Return the winner **from the version that closed**, not from the first read before retrying. An already-closed state has the answer for every future close. There is no window in which price or bidder can change separately from the closure decision.

For replay, include an immutable map from bid ID to `(bidder, amount, decision)` in that state. Look up the ID **before** checking whether bidding is closed. Replay matching terms, reject changed terms as a conflict, or compute a new decision. Publish the receipt and any leader change in the same CAS. A new declined ID still needs a receipt; closing must preserve all receipts. Keeping them costs memory but makes a retry unambiguous without waiting for another bidder.

**Test to keep:** start with Ada leading at 100. Release Bea's 120 bid and a close call together. Assert the *pair* of results: accepted/Bea at 120 or declined/Ada at 100. Asserting only “winner is either Ada or Bea” misses the original bug. Then retry Bea's exact request after close and require the original decision in either case. Use bounded waits in the test harness; the auction itself needs no locks or blocking waits.
