package com.fszuberski.multithreading.caching;

import com.fszuberski.multithreading.support.MutableClock;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Nested;

import java.time.Duration;
import java.util.Optional;

import static com.fszuberski.multithreading.support.TestThreads.race;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class ExpiringCacheTest {
    @Test
    void shouldExpireAndReplaceEntriesGivenInjectedClockAtTtlBoundary() {
        var clock = new MutableClock();
        var cache = new ExpiringCache<String, String>(Duration.ofSeconds(10), clock);
        assertEquals(Optional.empty(), cache.get("key"));
        cache.put("key", "old");
        clock.advance(Duration.ofSeconds(9));
        assertEquals(Optional.of("old"), cache.get("key"));
        clock.advance(Duration.ofSeconds(1));
        assertEquals(Optional.empty(), cache.get("key"));
        cache.put("key", "new");
        clock.advance(Duration.ofSeconds(9));
        assertEquals(Optional.of("new"), cache.get("key"));
    }

    @Test
    void shouldRetainIndependentEntriesGivenConcurrentKeys() throws Exception {
        var cache = new ExpiringCache<Long, Long>(Duration.ofMinutes(1), new MutableClock());
        race(8, () -> {
            long key = Thread.currentThread().threadId();
            cache.put(key, key);
            assertEquals(Optional.of(key), cache.get(key));
            return null;
        });
    }

    @Nested
    class InvalidEntries {
        @Test
        void shouldRejectTtlGivenZeroDuration() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ExpiringCache<>(Duration.ZERO, new MutableClock()));
        }

        @Test
        void shouldRejectEntryGivenNullValue() {
            var cache = new ExpiringCache<Long, Long>(Duration.ofMinutes(1), new MutableClock());
            assertThrows(NullPointerException.class, () -> cache.put(1L, null));
        }
    }
}
