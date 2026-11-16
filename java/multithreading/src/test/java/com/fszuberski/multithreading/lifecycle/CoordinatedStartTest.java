package com.fszuberski.multithreading.lifecycle;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static com.fszuberski.multithreading.support.TestThreads.await;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class CoordinatedStartTest {
    @Test
    void shouldRunAndJoinEveryWorkerGivenCoordinatedStart() throws Exception {
        var allRunning = new CountDownLatch(6);
        var calls = new AtomicIntegerArray(6);
        Set<Thread> workers = ConcurrentHashMap.newKeySet();
        new CoordinatedStart().run(6, index -> {
            workers.add(Thread.currentThread());
            assertFalse(Thread.currentThread().isVirtual());
            allRunning.countDown();
            try {
                await(allRunning);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(interrupted);
            }
            calls.incrementAndGet(index);
        });
        assertEquals(6, workers.size());
        workers.forEach(thread -> assertFalse(thread.isAlive(), "Workers must be joined"));
        for (int i = 0; i < 6; i++) {
            assertEquals(1, calls.get(i));
        }
    }

    @Test
    void shouldRejectWorkerCountGivenZeroWorkers() {
        assertThrows(IllegalArgumentException.class, () -> new CoordinatedStart().run(0, index -> {}));
    }

    @Test
    void shouldReportWorkerCauseGivenFailingWorker() {
        var failure = assertThrows(IllegalStateException.class, () -> new CoordinatedStart().run(3, index -> {
            if (index == 1) {
                throw new IllegalArgumentException("bad task");
            }
        }));
        assertInstanceOf(IllegalArgumentException.class, failure.getCause());
    }
}
