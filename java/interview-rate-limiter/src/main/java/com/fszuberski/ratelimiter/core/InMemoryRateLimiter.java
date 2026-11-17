package com.fszuberski.ratelimiter.core;

import java.time.Clock;
import java.time.Duration;

/** Interview placeholder, NOT a production limiter. No algorithm or client state yet. */
public final class InMemoryRateLimiter implements RateLimiter {
    private final LimiterSettings settings;
    private final Clock clock;

    public InMemoryRateLimiter(LimiterSettings settings, Clock clock) {
        this.settings = settings;
        this.clock = clock;
    }

    @Override
    public Decision acquire(String clientKey) {
        // TODO: implement a per-client token bucket OR sliding window using settings and clock.
        return new Decision(true, Duration.ZERO);
    }

    @Override
    public int cleanup() {
        // TODO: safely evict idle state without resetting an active client's quota.
        return 0;
    }
}
