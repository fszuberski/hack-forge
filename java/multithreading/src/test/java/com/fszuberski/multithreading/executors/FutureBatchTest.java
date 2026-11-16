package com.fszuberski.multithreading.executors;

import com.fszuberski.multithreading.support.TestThreads;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Nested;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.fszuberski.multithreading.support.TestThreads.await;

import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class FutureBatchTest {
    @Nested
    class CompletedResults {
        @Test
        void shouldRetainResultOrderGivenCompletedFuturesAndZeroBudget() throws Exception {
            assertEquals(List.of(2, 1), new FutureBatch().collect(List.of(
                    CompletableFuture.completedFuture(2), CompletableFuture.completedFuture(1)), Duration.ZERO));
        }
    }

    @Nested
    class Cancellation {
        @Test
        void shouldCancelUnfinishedSiblingsGivenFailedFuture() {
            var pending = new CompletableFuture<Integer>();
            var failed = CompletableFuture.<Integer>failedFuture(new IllegalArgumentException("service unavailable"));
            var failure = assertThrows(ExecutionException.class,
                    () -> new FutureBatch().collect(List.of(failed, pending), Duration.ofSeconds(1)));
            assertInstanceOf(IllegalArgumentException.class, failure.getCause());
            assertTrue(pending.isCancelled());
        }

        @Test
        void shouldCancelUnfinishedFuturesGivenExpiredTimeout() {
            var first = new CompletableFuture<Integer>();
            var second = new CompletableFuture<Integer>();
            assertThrows(TimeoutException.class,
                    () -> new FutureBatch().collect(List.of(first, second), Duration.ofMillis(30)));
            assertTrue(first.isCancelled());
            assertTrue(second.isCancelled());
        }

        @Test
        void shouldCancelUnfinishedFuturesGivenInterruptedCaller() throws Exception {
            var pending = new CompletableFuture<Integer>();
            var entered = new CountDownLatch(1);
            var propagated = new CountDownLatch(1);
            try (var threads = new TestThreads(1)) {
                var collector = threads.submit(() -> {
                    entered.countDown();
                    try {
                        new FutureBatch().collect(List.of(pending), Duration.ofHours(1));
                        fail("Collection should be interrupted");
                    } catch (InterruptedException expected) {
                        propagated.countDown();
                    }
                    return null;
                });
                try {
                    await(entered);
                } finally {
                    collector.cancel(true);
                }
                await(propagated);
                assertTrue(pending.isCancelled());
            }
        }
    }
}
