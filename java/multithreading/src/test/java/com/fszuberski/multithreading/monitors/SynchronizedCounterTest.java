package com.fszuberski.multithreading.monitors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static com.fszuberski.multithreading.support.TestThreads.race;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("exercise")
@Timeout(12)
class SynchronizedCounterTest {
    @Test
    void shouldRetainEveryIncrementGivenConcurrentWritersAndInitialZero() throws Exception {
        var counter = new SynchronizedCounter();
        assertEquals(0, counter.value());
        race(8, () -> {
            for (int i = 0; i < 10_000; i++) {
                counter.increment();
            }
            return null;
        });
        assertEquals(80_000, counter.value());
    }
}
