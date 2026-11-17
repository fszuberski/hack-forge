package com.fszuberski.ratelimiter.core;

public interface RateLimiter {
    /** Atomically attempts to consume one permit for a normalized, nonblank client key. */
    Decision acquire(String clientKey);

    /** Removes safely expired client state using the injected clock; returns entries removed. */
    int cleanup();
}
