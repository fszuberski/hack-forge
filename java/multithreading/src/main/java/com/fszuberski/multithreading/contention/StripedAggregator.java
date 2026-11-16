package com.fszuberski.multithreading.contention;

import java.util.function.LongUnaryOperator;

/** Exercise 15B. Per-stripe synchronization; updates on distinct stripes can overlap. */
public final class StripedAggregator {
    public StripedAggregator(int stripes) {
        // TODO 15B: positive stripe count, independent locks and safely partitioned state.
    }

    public long update(String key, LongUnaryOperator operation) {
        throw new UnsupportedOperationException("TODO 15B: exactly-once callback under the key's stripe lock");
    }

    public long get(String key) {
        throw new UnsupportedOperationException("TODO 15B: absent key starts at zero");
    }
}
