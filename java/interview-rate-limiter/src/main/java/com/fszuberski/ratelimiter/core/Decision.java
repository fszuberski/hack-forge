package com.fszuberski.ratelimiter.core;

import java.time.Duration;

/**
 * Allowed decisions have zero retryAfter. Denied decisions have a positive duration
 * until one permit could next be available, assuming no competing requests.
 */
public record Decision(boolean allowed, Duration retryAfter) {
}
