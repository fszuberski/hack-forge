package com.fszuberski.ratelimiter.testing.data;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

/** Owned by each scenario; advancing time never sleeps or changes a global clock. */
public final class MutableClockData extends Clock {
    private final AtomicReference<Instant> now;
    private final ZoneId zone;

    public MutableClockData() {
        this(new AtomicReference<>(Instant.parse("2026-01-01T00:00:00Z")), ZoneOffset.UTC);
    }

    private MutableClockData(AtomicReference<Instant> now, ZoneId zone) {
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
        return new MutableClockData(now, zone);
    }

    @Override
    public Instant instant() {
        return now.get();
    }
}
