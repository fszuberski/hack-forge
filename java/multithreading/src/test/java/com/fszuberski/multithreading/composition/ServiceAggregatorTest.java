package com.fszuberski.multithreading.composition;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.fszuberski.multithreading.support.TestThreads.result;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class ServiceAggregatorTest {
    @Test
    void shouldCompleteOnlyAfterBothInputsGivenPendingServices() throws Exception {
        var profile = new CompletableFuture<String>();
        var balance = new CompletableFuture<Integer>();
        var dashboard = new ServiceAggregator().aggregate(profile, balance);
        assertFalse(dashboard.isDone());
        balance.complete(42);
        assertFalse(dashboard.isDone());
        profile.complete("Ada");
        assertEquals(new ServiceAggregator.Dashboard("Ada", 42), result(dashboard));
    }

    @Test
    void shouldPreserveFailureAndInputsGivenFailedService() {
        var cause = new IllegalArgumentException("profile missing");
        var profile = CompletableFuture.<String>failedFuture(cause);
        var balance = CompletableFuture.completedFuture(42);
        var dashboard = new ServiceAggregator().aggregate(profile, balance);
        var failure = assertThrows(ExecutionException.class, () -> result(dashboard));
        assertSame(cause, failure.getCause());
        assertEquals(42, balance.join());
    }
}
