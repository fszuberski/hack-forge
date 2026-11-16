package com.fszuberski.multithreading.collections;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Map;

import static com.fszuberski.multithreading.support.TestThreads.race;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class FrequencyTrackerTest {
    @Test
    void shouldRetainUpdatesAndDetachSnapshotsGivenConcurrentRecording() throws Exception {
        var tracker = new FrequencyTracker();
        race(8, () -> {
            for (int i = 0; i < 1_000; i++) {
                tracker.recordAll(List.of("java", "java", "locks"));
            }
            return null;
        });
        var snapshot = tracker.snapshot();
        assertEquals(Map.of("java", 16_000L, "locks", 8_000L), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.put("new", 1L));
        tracker.recordAll(List.of("java"));
        assertEquals(16_000L, snapshot.get("java"));
        assertEquals(16_001L, tracker.snapshot().get("java"));
    }
}
