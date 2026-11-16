package com.fszuberski.multithreading.cancellation;

import com.fszuberski.multithreading.support.TestThreads;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class GracefulShutdownTest {
    @Test
    void shouldFinishWorkAndTerminateExecutorGivenSufficientGraceBudget() throws Exception {
        var executor = Executors.newSingleThreadExecutor(daemonFactory("shutdown"));
        var finished = executor.submit(() -> 42);
        try {
            assertTrue(new GracefulShutdown().shutdown(executor, WAIT, WAIT));
            assertTrue(executor.isTerminated());
            assertEquals(42, result(finished));
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS));
        }
    }

    @Test
    void shouldInterruptWorkGivenExpiredGraceBudget() throws Exception {
        var executor = Executors.newSingleThreadExecutor(daemonFactory("shutdown"));
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var interrupted = new AtomicBoolean();
        executor.submit(() -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException expected) {
                interrupted.set(true);
                Thread.currentThread().interrupt();
            }
        });
        try {
            await(entered);
            assertTrue(new GracefulShutdown().shutdown(executor, Duration.ZERO, WAIT));
            assertTrue(interrupted.get());
            assertTrue(executor.isTerminated());
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS));
        }
    }

    @Test
    void shouldForceShutdownAndPropagateInterruptionGivenInterruptedCaller() throws Exception {
        var executor = Executors.newSingleThreadExecutor(daemonFactory("shutdown"));
        var taskStarted = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var shuttingDown = new CountDownLatch(1);
        var propagated = new CountDownLatch(1);
        executor.submit(() -> {
            taskStarted.countDown();
            try {
                release.await();
            } catch (InterruptedException expected) {
                Thread.currentThread().interrupt();
            }
        });
        try (var threads = new TestThreads(1)) {
            await(taskStarted);
            var shutdown = threads.submit(() -> {
                shuttingDown.countDown();
                try {
                    new GracefulShutdown().shutdown(executor, Duration.ofHours(1), WAIT);
                    fail("Shutdown wait should be interrupted");
                } catch (InterruptedException expected) {
                    propagated.countDown();
                }
                return null;
            });
            try {
                await(shuttingDown);
            } finally {
                shutdown.cancel(true);
            }
            await(propagated);
            assertTrue(executor.isShutdown());
            assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS));
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS));
        }
    }
}
