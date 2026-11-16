package com.fszuberski.multithreading.contention;

/** Exercise 15A. Repair caller-order locking by imposing one global account-index order. */
public final class DeadlockFreeLedger {
    public DeadlockFreeLedger(int accounts, long openingBalance) {
        // TODO 15A: one balance and one interruptible lock per account.
    }

    public boolean transfer(int from, int to, long amount) throws InterruptedException {
        throw new UnsupportedOperationException("TODO 15A: consistent lock order, no partial transfer");
    }

    public long[] balances() throws InterruptedException {
        throw new UnsupportedOperationException("TODO 15A: consistent snapshot in the same lock order");
    }
}
