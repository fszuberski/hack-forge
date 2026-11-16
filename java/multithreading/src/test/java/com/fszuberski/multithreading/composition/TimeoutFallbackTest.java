package com.fszuberski.multithreading.composition;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Nested;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class TimeoutFallbackTest {
    private ScheduledThreadPoolExecutor scheduler() {
        var scheduler = new ScheduledThreadPoolExecutor(1, daemonFactory("fallback-timer"));
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }

    @Nested
    class ExpiredTimeout {
        @Test
        void shouldUseFallbackOnceWithoutChangingSourceGivenExpiredTimeout() throws Exception {
            var scheduler = scheduler();
            try {
                var source = new CompletableFuture<String>();
                var calls = new AtomicInteger();
                var result = new TimeoutFallback().withFallback(source, Duration.ZERO, () -> {
                    calls.incrementAndGet();
                    return "cached";
                }, scheduler);
                assertEquals("cached", result(result));
                assertEquals(1, calls.get());
                assertFalse(source.isDone());
                source.complete("late");
                assertEquals("cached", result(result));
                assertFalse(scheduler.isShutdown());
            } finally {
                scheduler.shutdownNow();
                assertTrue(scheduler.awaitTermination(3, TimeUnit.SECONDS));
            }
        }

        @Test
        void shouldCompleteExceptionallyGivenThrowingFallback() throws Exception {
            var scheduler = scheduler();
            try {
                var cause = new IllegalArgumentException("fallback failed");
                var wrapper = new TimeoutFallback().withFallback(new CompletableFuture<String>(), Duration.ZERO,
                        () -> { throw cause; }, scheduler);
                assertSame(cause, assertThrows(ExecutionException.class, () -> result(wrapper)).getCause());
            } finally {
                scheduler.shutdownNow();
                assertTrue(scheduler.awaitTermination(3, TimeUnit.SECONDS));
            }
        }
    }

    @Nested
    class TimerCleanup {
        @Test
        void shouldCancelTimerWithoutFallbackGivenSuccessfulSource() throws Exception {
            var scheduler = scheduler();
            try {
                var source = new CompletableFuture<String>();
                var result = new TimeoutFallback().withFallback(source, Duration.ofHours(1), () -> {
                    fail("No fallback for a successful source");
                    return "wrong";
                }, scheduler);
                source.complete("live");
                assertEquals("live", result(result));
                awaitCondition(() -> scheduler.getQueue().isEmpty());
            } finally {
                scheduler.shutdownNow();
                assertTrue(scheduler.awaitTermination(3, TimeUnit.SECONDS));
            }
        }

        @Test
        void shouldPreserveFailureAndCancelTimerGivenFailedSource() throws Exception {
            var scheduler = scheduler();
            try {
                var cause = new IllegalArgumentException("failed");
                var source = CompletableFuture.<String>failedFuture(cause);
                var result = new TimeoutFallback().withFallback(source, Duration.ofHours(1), () -> "wrong", scheduler);
                assertSame(cause, assertThrows(ExecutionException.class, () -> result(result)).getCause());
                awaitCondition(() -> scheduler.getQueue().isEmpty());
            } finally {
                scheduler.shutdownNow();
                assertTrue(scheduler.awaitTermination(3, TimeUnit.SECONDS));
            }
        }

        @Test
        void shouldCancelTimerWithoutCancellingSourceGivenCancelledWrapper() throws Exception {
            var scheduler = scheduler();
            try {
                var source = new CompletableFuture<String>();
                var wrapper = new TimeoutFallback().withFallback(source, Duration.ofHours(1), () -> "wrong", scheduler);
                assertTrue(wrapper.cancel(true));
                assertFalse(source.isDone());
                awaitCondition(() -> scheduler.getQueue().isEmpty());
            } finally {
                scheduler.shutdownNow();
                assertTrue(scheduler.awaitTermination(3, TimeUnit.SECONDS));
            }
        }
    }
}
