package com.fszuberski.multithreading.atomics;

/** Exercise 05B. Compare AtomicLong and LongAdder semantics; do not benchmark with JUnit. */
public final class MetricCounters {
    public void incrementAtomic() {
        throw new UnsupportedOperationException("TODO 05B: AtomicLong");
    }

    public void incrementScalable() {
        throw new UnsupportedOperationException("TODO 05B: LongAdder");
    }

    public long atomicValue() {
        throw new UnsupportedOperationException("TODO 05B: linearizable read");
    }

    public long scalableValue() {
        throw new UnsupportedOperationException("TODO 05B: sum stripes");
    }
}
