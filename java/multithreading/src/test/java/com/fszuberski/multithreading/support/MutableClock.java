package com.fszuberski.multithreading.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

/** Deterministic expiration tests without waiting for wall-clock time. */
public final class MutableClock extends Clock {
    private final AtomicReference<Instant> now;
    private final ZoneId zone;

    public MutableClock() {
        this(new AtomicReference<>(Instant.parse("2025-01-01T00:00:00Z")), ZoneOffset.UTC);
    }

    private MutableClock(AtomicReference<Instant> now, ZoneId zone) {
        this.now = now;
        this.zone = zone;
    }

    public void advance(Duration duration) {
        now.updateAndGet(instant -> instant.plus(duration));
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(now, zone);
    }

    @Override
    public Instant instant() {
        return now.get();
    }
}
