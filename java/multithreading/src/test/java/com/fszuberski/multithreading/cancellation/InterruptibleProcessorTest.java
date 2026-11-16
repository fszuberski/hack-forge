package com.fszuberski.multithreading.cancellation;

import com.fszuberski.multithreading.support.TestThreads;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class InterruptibleProcessorTest {
    @Test
    void shouldHandleInputAndPropagateInterruptionGivenCancellationWhileWaiting() throws Exception {
        var queue = new LinkedBlockingQueue<>(List.of("first"));
        var handled = new CountDownLatch(1);
        var stopped = new CountDownLatch(1);
        var propagated = new AtomicBoolean();
        try (var threads = new TestThreads(1)) {
            var worker = threads.submit(() -> {
                try {
                    new InterruptibleProcessor().process(queue, item -> {
                        assertEquals("first", item);
                        handled.countDown();
                    });
                } catch (InterruptedException interrupted) {
                    propagated.set(true);
                } finally {
                    stopped.countDown();
                }
                return null;
            });
            try {
                await(handled);
            } finally {
                worker.cancel(true);
            }
            await(stopped);
            assertTrue(propagated.get(), "Must propagate InterruptedException, not return normally");
        }
    }

    @Test
    void shouldSkipProcessingGivenAlreadyInterruptedCaller() throws Exception {
        try (var threads = new TestThreads(1)) {
            result(threads.submit(() -> {
                Thread.currentThread().interrupt();
                try {
                    assertThrows(InterruptedException.class, () -> new InterruptibleProcessor().process(
                            new LinkedBlockingQueue<>(List.of("first")), item -> fail("Must not process")));
                } finally {
                    Thread.interrupted();
                }
                return null;
            }));
        }
    }
}
