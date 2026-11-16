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
class ShutdownSignalTest {
    @Test
    void shouldRemainRequestedGivenRepeatedRequestsAfterInitialFalseState() {
        var signal = new ShutdownSignal();
        assertFalse(signal.isRequested());
        signal.request();
        signal.request();
        assertTrue(signal.isRequested());
    }

    @Test
    void shouldExposeRequestToReaderGivenNoPostPublicationGate() throws Exception {
        var signal = new ShutdownSignal();
        try (var threads = new TestThreads(1)) {
            var reading = new CountDownLatch(1);
            var reader = threads.submit(() -> {
                reading.countDown();
                // Bounded test polling, not the recommended production waiting strategy.
                awaitCondition(signal::isRequested);
                return true;
            });
            await(reading);
            signal.request();
            assertTrue(result(reader));
        }
    }
}
