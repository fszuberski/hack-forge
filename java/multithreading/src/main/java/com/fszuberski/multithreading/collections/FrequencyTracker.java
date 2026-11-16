package com.fszuberski.multithreading.collections;

import java.util.List;
import java.util.Map;

/** Exercise 06A. Concurrent per-key counting; snapshots must not expose live mutable state. */
public final class FrequencyTracker {
    public void recordAll(List<String> words) {
        throw new UnsupportedOperationException("TODO 06A: concurrent frequency updates");
    }

    public Map<String, Long> snapshot() {
        throw new UnsupportedOperationException("TODO 06A: immutable observational snapshot");
    }
}
