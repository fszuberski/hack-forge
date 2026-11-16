package com.fszuberski.multithreading.lifecycle;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Nested;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.fszuberski.multithreading.support.TestThreads.await;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class CancellableWorkerTest {
    @Nested
    class Stopping {
        @Test
        void shouldInterruptBlockingWorkAndStopIdempotentlyGivenRepeatedStopRequests() throws Exception {
            var entered = new CountDownLatch(1);
            var neverReleased = new CountDownLatch(1);
            var interrupted = new AtomicBoolean();
            var worker = new CancellableWorker(() -> {
                entered.countDown();
                try {
                    neverReleased.await();
                } catch (InterruptedException expected) {
                    interrupted.set(true);
                    throw expected;
                }
            });
            worker.start();
            try {
                await(entered);
                assertFalse(worker.awaitStopped(Duration.ZERO));
            } finally {
                worker.stop();
                worker.stop();
                try {
                    assertTrue(worker.awaitStopped(Duration.ofSeconds(3)));
                    assertTrue(interrupted.get(), "Cancellation must interrupt blocking work");
                } finally {
                    neverReleased.countDown();
                }
            }
        }
    }

    @Nested
    class Starting {
        @Test
        void shouldRepeatWorkAndRejectSecondStartGivenRunningWorker() throws Exception {
            var iterations = new AtomicInteger();
            var ran = new CountDownLatch(10);
            var worker = new CancellableWorker(() -> {
                iterations.incrementAndGet();
                ran.countDown();
                Thread.yield();
            });
            worker.start();
            try {
                await(ran);
                assertThrows(IllegalStateException.class, worker::start);
                assertTrue(iterations.get() >= 10);
            } finally {
                worker.stop();
                assertTrue(worker.awaitStopped(Duration.ofSeconds(3)));
            }
        }
    }
}
