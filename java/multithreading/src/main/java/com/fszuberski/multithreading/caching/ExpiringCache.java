package com.fszuberski.multithreading.caching;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

/** Exercise 14B. Immutable entries and lazy expiry; use the supplied clock, never sleep. */
public final class ExpiringCache<K, V> {
    public ExpiringCache(Duration ttl, Clock clock) {
        // TODO 14B: validate positive TTL and retain the clock.
    }

    public void put(K key, V value) {
        throw new UnsupportedOperationException("TODO 14B: atomic entry replacement");
    }

    public Optional<V> get(K key) {
        throw new UnsupportedOperationException("TODO 14B: conditional removal of expired entry");
    }
}
