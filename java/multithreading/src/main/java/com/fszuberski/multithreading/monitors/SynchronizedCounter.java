package com.fszuberski.multithreading.monitors;

/** Exercise 03A. Protect both reads and writes using the same intrinsic lock. */
public final class SynchronizedCounter {
    public void increment() {
        throw new UnsupportedOperationException("TODO 03A: atomic read-modify-write");
    }

    public long value() {
        throw new UnsupportedOperationException("TODO 03A: visibility-safe read");
    }
}
