package com.fszuberski.multithreading.caching;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class SingleFlightTest {
    @Nested
    class LoadSharing {
        @Test
        void shouldShareOneLoadWithoutSharingCancellationGivenSimultaneousCallers() throws Exception {
            var flight = new SingleFlight<String, String>();
            var source = new CompletableFuture<String>();
            var loads = new AtomicInteger();
            var callers = race(16, () -> flight.execute("key", () -> {
                loads.incrementAndGet();
                return source;
            }));
            assertEquals(1, loads.get());
            callers.getFirst().cancel(true);
            assertFalse(source.isCancelled());
            source.complete("value");
            for (var caller : callers.subList(1, callers.size())) {
                assertEquals("value", result(caller));
            }
            assertEquals("fresh", result(flight.execute("key", () -> CompletableFuture.completedFuture("fresh"))));
        }

        @Test
        void shouldKeepResultsIndependentGivenDistinctKeys() throws Exception {
            var flight = new SingleFlight<String, Integer>();
            var pending = new CompletableFuture<Integer>();
            var first = flight.execute("a", () -> pending);
            assertEquals(2, result(flight.execute("b", () -> CompletableFuture.completedFuture(2))));
            assertFalse(first.isDone());
            pending.complete(1);
            assertEquals(1, result(first));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldEvictFailedLoadsGivenConsecutiveLoaderFailures(boolean synchronousFirst) throws Exception {
        var flight = new SingleFlight<String, String>();
        // Consecutive failures on one key test eviction across state transitions, not isolated loads.
        for (boolean synchronous : new boolean[]{synchronousFirst, !synchronousFirst}) {
            RuntimeException cause = synchronous ? new IllegalArgumentException("load failed")
                    : new IllegalStateException("offline");
            var failed = flight.execute("key", () -> {
                if (synchronous) {
                    throw cause;
                }
                return CompletableFuture.failedFuture(cause);
            });
            assertInstanceOf(cause.getClass(),
                    assertThrows(ExecutionException.class, () -> result(failed)).getCause());
        }
        assertEquals("retry", result(flight.execute("key", () -> CompletableFuture.completedFuture("retry"))));
    }
}
