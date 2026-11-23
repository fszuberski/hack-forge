# Debrief: transfer the last €80

A successful transfer changes two balances, preserves their sum and never overdraws the source. An observer must see either the balances before the transfer or the balances after it. A retry also needs the *original decision*, not a new check against today's balance.

## First green test

After validating the instructions, two writes suffice in a sequential ledger:

```java
long available = balances.get(from);
if (available < amount) return false;
balances.put(from, available - amount);
balances.put(to, balances.get(to) + amount);
return true;
```

A has 100; B and C have zero. Both workers read 100 from A. Each writes 20 back and credits its recipient with 80. Both claim success and the ledger now totals 180. A CAS debit on A fixes that overdraft race but leaves a different hole: a snapshot between debit and credit sees money missing.

## Pick a boundary that contains both accounts

For a small ledger, keep an `AtomicReference` to an immutable map of balances. Read a version, validate both accounts and sufficient funds, compute both replacements in a private copy, and publish it with one compare-and-set (CAS). If it fails, retry *all* balance checks. A snapshot returns the immutable map from one reference read. Opposing transfers contend on that version rather than holding account locks; a stalled caller cannot block the rest. The cost is a map copy and possible retries even for disjoint accounts.

For task 4 expand the published state:

```java
record Instruction(String from, String to, long cents) {}
record Receipt(Instruction instruction, boolean accepted) {}
record State(Map<String, Long> balances, Map<String, Receipt> receipts) {
    State {
        balances = Map.copyOf(balances);
        receipts = Map.copyOf(receipts);
    }
}
```

Check the ID first. If present, compare the full instruction and replay or report conflict. Otherwise build a receipt from this version's decision. Include it in the **same CAS** as the balance change; even insufficient funds must publish a receipt, with unchanged balances. For a transfer without an ID, use the same ledger state but omit the receipt. No part of a transfer may mutate the old version.

**Test to keep:** decline a valid instruction for insufficient funds, top up its source using a transfer *from another account*, and retry the original ID. It must still decline; a fresh ID may now succeed. For the overdraft race, check both return values and every final balance: conservation alone cannot detect accidentally rejecting both transfers.
