package com.fszuberski.multithreading.composition;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/** Exercise 10B. Race a source against a timer without mutating the caller's future. */
public final class TimeoutFallback {
    public <T> CompletableFuture<T> withFallback(CompletableFuture<T> source, Duration timeout,
                                                Supplier<T> fallback, ScheduledExecutorService scheduler) {
        throw new UnsupportedOperationException("TODO 10B: independent result and timer cleanup");
    }
}
