package com.fszuberski.multithreading.contention;

import com.fszuberski.multithreading.support.TestThreads;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class StripedAggregatorTest {
    @Test
    void shouldApplyCallbacksExactlyOnceGivenConcurrentSameKeyUpdates() throws Exception {
        var aggregator = new StripedAggregator(16);
        var callbacks = new AtomicInteger();
        race(8, () -> {
            for (int i = 0; i < 1_000; i++) {
                aggregator.update("account", value -> {
                    callbacks.incrementAndGet();
                    return value + 1;
                });
            }
            return null;
        });
        assertEquals(8_000, aggregator.get("account"));
        assertEquals(8_000, callbacks.get());
        assertEquals(0, aggregator.get("absent"));
    }

    @Test
    void shouldAllowIndependentProgressGivenSlowCallbackOnAnotherStripe() throws Exception {
        var aggregator = new StripedAggregator(16);
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        try (var threads = new TestThreads(2)) {
            var slow = threads.submit(() -> aggregator.update("a", value -> {
                entered.countDown();
                try {
                    await(release);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(interrupted);
                }
                return value + 1;
            }));
            try {
                await(entered);
                assertEquals(1, result(threads.submit(() -> aggregator.update("b", value -> value + 1))));
            } finally {
                release.countDown();
            }
            assertEquals(1, result(slow));
        }
    }
}
