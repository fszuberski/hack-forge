package com.fszuberski.multithreading.composition;

import java.util.concurrent.CompletableFuture;

/** Exercise 10A. Compose asynchronous values without join/get or an extra executor. */
public final class ServiceAggregator {
    public record Dashboard(String profile, int balance) {}

    public CompletableFuture<Dashboard> aggregate(CompletableFuture<String> profile,
                                                   CompletableFuture<Integer> balance) {
        throw new UnsupportedOperationException("TODO 10A: non-blocking composition");
    }
}
