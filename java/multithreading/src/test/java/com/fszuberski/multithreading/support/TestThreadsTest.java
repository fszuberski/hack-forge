package com.fszuberski.multithreading.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(12)
class TestThreadsTest {
    @Nested
    class Racing {
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 8})
        void shouldReturnEveryLogicalIndexInOrderGivenParticipantCount(int participants) throws Exception {
            assertEquals(IntStream.range(0, participants).boxed().toList(),
                    raceIndexed(participants, index -> () -> index));
        }

        @Test
        void shouldUseUniqueDaemonWorkersGivenConcurrentParticipants() throws Exception {
            var workers = race(8, () -> Thread.currentThread());
            assertEquals(8, new HashSet<>(workers).size());
            assertEquals(8, workers.stream().map(Thread::getName).distinct().count());
            workers.forEach(worker -> {
                assertTrue(worker.isDaemon());
                assertFalse(worker.isAlive());
            });
        }

        @Test
        void shouldPropagateCauseGivenFailingParticipant() {
            var cause = new IllegalArgumentException("fixture failure");
            assertSame(cause, assertThrows(ExecutionException.class, () -> race(2, () -> {
                throw cause;
            })).getCause());
        }
    }

    @Nested
    class CleanupAndWaiting {
        @Test
        void shouldJoinWorkersAndPreserveInterruptionGivenInterruptedOwner() throws Exception {
            try (var owner = new TestThreads(1)) {
                result(owner.submit(() -> {
                    var threads = new TestThreads(1);
                    var worker = result(threads.submit(() -> Thread.currentThread()));
                    Thread.currentThread().interrupt();
                    try {
                        assertThrows(InterruptedException.class, threads::close);
                        assertTrue(Thread.currentThread().isInterrupted());
                        assertFalse(worker.isAlive());
                    } finally {
                        Thread.interrupted();
                        threads.close();
                    }
                    return null;
                }));
            }
        }

        @Test
        void shouldInterruptAndJoinWorkerGivenCloseDuringBlockedTask() throws Exception {
            var entered = new CountDownLatch(1);
            var stopped = new CompletableFuture<Boolean>();
            Thread worker;
            try (var threads = new TestThreads(1)) {
                worker = result(threads.submit(() -> Thread.currentThread()));
                threads.submit(() -> {
                    entered.countDown();
                    try {
                        new CountDownLatch(1).await();
                        stopped.complete(false);
                    } catch (InterruptedException expected) {
                        stopped.complete(true);
                        Thread.currentThread().interrupt();
                    }
                    return null;
                });
                await(entered);
            }
            assertTrue(result(stopped));
            assertFalse(worker.isAlive());
        }

        @Test
        void shouldObserveConditionGivenWorkerPublication() throws Exception {
            var published = new CompletableFuture<Boolean>();
            try (var threads = new TestThreads(1)) {
                var task = threads.submit(() -> published.complete(true));
                awaitCondition(published::isDone);
                assertTrue(result(published));
                assertTrue(result(task));
            }
        }

        @Test
        void shouldAbortPollingAndRetainInterruptGivenInterruptedWorker() throws Exception {
            try (var threads = new TestThreads(1)) {
                result(threads.submit(() -> {
                    Thread.currentThread().interrupt();
                    try {
                        assertThrows(AssertionError.class, () -> awaitCondition(() -> false));
                        assertTrue(Thread.currentThread().isInterrupted());
                    } finally {
                        Thread.interrupted();
                    }
                    return null;
                }));
            }
        }
    }
}
