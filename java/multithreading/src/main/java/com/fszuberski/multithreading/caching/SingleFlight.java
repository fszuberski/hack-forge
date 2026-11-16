package com.fszuberski.multithreading.caching;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/** Exercise 14A. Share in-flight loads, not a permanent cache or callers' cancellation state. */
public final class SingleFlight<K, V> {
    public CompletableFuture<V> execute(K key, Supplier<CompletableFuture<V>> loader) {
        throw new UnsupportedOperationException("TODO 14A: placeholder before load, conditional eviction on completion");
    }
}
