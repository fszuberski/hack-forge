package com.fszuberski.multithreading.memory;

import com.fszuberski.multithreading.support.TestThreads;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class PublishedConfigTest {
    @Test
    void shouldPublishWholeSnapshotsGivenConcurrentReplacement() throws Exception {
        var config = new PublishedConfig();
        assertNull(config.current());
        config.publish("pool-0", 0);
        try (var threads = new TestThreads(2)) {
            var start = new CountDownLatch(1);
            var writer = threads.submit(() -> {
                await(start);
                for (int i = 1; i <= 10_000; i++) {
                    config.publish("pool-" + i, i);
                }
                return null;
            });
            var reader = threads.submit(() -> {
                await(start);
                for (int i = 0; i < 10_000; i++) {
                    var snapshot = config.current();
                    assertEquals("pool-" + snapshot.maxConnections(), snapshot.endpoint());
                }
                return null;
            });
            start.countDown();
            result(writer);
            result(reader);
        }
        assertEquals(new PublishedConfig.Config("pool-10000", 10_000), config.current());
    }

    @Test
    void shouldKeepPreviousSnapshotUnchangedGivenReplacement() {
        var config = new PublishedConfig();
        config.publish("first", 2);
        var old = config.current();
        config.publish("second", 3);
        assertEquals(new PublishedConfig.Config("first", 2), old);
    }
}
