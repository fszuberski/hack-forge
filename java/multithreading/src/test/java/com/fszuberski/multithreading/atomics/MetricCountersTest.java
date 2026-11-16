package com.fszuberski.multithreading.atomics;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static com.fszuberski.multithreading.support.TestThreads.race;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("exercise")
@Timeout(12)
class MetricCountersTest {
    @Test
    void shouldReturnExactTotalsGivenCompletedConcurrentWriters() throws Exception {
        var counters = new MetricCounters();
        assertEquals(0, counters.atomicValue());
        assertEquals(0, counters.scalableValue());
        race(8, () -> {
            for (int i = 0; i < 10_000; i++) {
                counters.incrementAtomic();
                counters.incrementScalable();
            }
            return null;
        });
        assertEquals(80_000, counters.atomicValue());
        assertEquals(80_000, counters.scalableValue());
    }
}
