package com.fszuberski.ratelimiter.core;

import java.time.Duration;

/** capacity > 0, window > 0, idleTtl >= window. Validate if making these configurable. */
public record LimiterSettings(int capacity, Duration window, Duration idleTtl) {
}
