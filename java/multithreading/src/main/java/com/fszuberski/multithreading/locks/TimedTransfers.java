package com.fszuberski.multithreading.locks;

import java.time.Duration;
import java.util.concurrent.locks.ReentrantLock;

/** Exercise 04A. One timeout budget across both lock acquisitions. */
public final class TimedTransfers {
    public static final class Account {
        final ReentrantLock lock = new ReentrantLock();
        long balance;

        public Account(long balance) {
            if (balance < 0) {
                throw new IllegalArgumentException("negative opening balance");
            }
            this.balance = balance;
        }

        public long balance() {
            lock.lock();
            try {
                return balance;
            } finally {
                lock.unlock();
            }
        }
    }

    public boolean transfer(Account from, Account to, long amount, Duration timeout)
            throws InterruptedException {
        throw new UnsupportedOperationException("TODO 04A: timed, atomic transfer with rollback-free failure");
    }
}
