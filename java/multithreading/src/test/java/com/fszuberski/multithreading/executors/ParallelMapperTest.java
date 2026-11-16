package com.fszuberski.multithreading.executors;

import com.fszuberski.multithreading.support.TestThreads;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Nested;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class ParallelMapperTest {
    @Test
    void shouldBoundParallelismAndPreserveOrderGivenConcurrentMapping() throws Exception {
        var entered = new CountDownLatch(2);
        var release = new CountDownLatch(1);
        var active = new AtomicInteger();
        var maximum = new AtomicInteger();
        try (var threads = new TestThreads(1)) {
            var result = threads.submit(() -> new ParallelMapper().map(List.of(1, 2, 3, 4), value -> {
                int count = active.incrementAndGet();
                maximum.accumulateAndGet(count, Math::max);
                entered.countDown();
                try {
                    await(release);
                    return value * 10;
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(interrupted);
                } finally {
                    active.decrementAndGet();
                }
            }, 2));
            try {
                await(entered);
                assertEquals(2, maximum.get());
            } finally {
                release.countDown();
            }
            assertEquals(List.of(10, 20, 30, 40), result(result));
            assertEquals(2, maximum.get());
        }
    }

    @Test
    void shouldRejectParallelismGivenZeroParallelism() {
        assertThrows(IllegalArgumentException.class, () -> new ParallelMapper().map(List.of(), value -> value, 0));
    }

    @Test
    void shouldAvoidCallbacksGivenEmptyInput() throws Exception {
        assertEquals(List.of(), new ParallelMapper().map(List.of(), value -> {
            fail("No callback for an empty input");
            return value;
        }, 2));
    }

    @Nested
    class TaskFailures {
        @Test
        void shouldPropagateTaskFailureGivenThrowingMapper() {
            var mapper = new ParallelMapper();
            var failure = assertThrows(ExecutionException.class,
                    () -> mapper.map(List.of(1), value -> {
                        throw new IllegalArgumentException("upstream failed");
                    }, 1));
            assertInstanceOf(IllegalArgumentException.class, failure.getCause());
        }

        @Test
        void shouldCancelEarlierBlockingSiblingGivenLaterTaskFailure() throws Exception {
            var started = new CountDownLatch(1);
            var stopped = new CountDownLatch(1);
            var release = new CountDownLatch(1);
            var interrupted = new AtomicBoolean();
            try {
                var failure = assertThrows(ExecutionException.class,
                        () -> new ParallelMapper().map(List.of(0, 1), index -> {
                            if (index == 1) {
                                try {
                                    await(started);
                                } catch (InterruptedException unexpected) {
                                    Thread.currentThread().interrupt();
                                    throw new IllegalStateException(unexpected);
                                }
                                throw new IllegalArgumentException("second task failed");
                            }
                            started.countDown();
                            try {
                                release.await();
                                return index;
                            } catch (InterruptedException expected) {
                                interrupted.set(true);
                                Thread.currentThread().interrupt();
                                throw new IllegalStateException(expected);
                            } finally {
                                stopped.countDown();
                            }
                        }, 2));
                assertInstanceOf(IllegalArgumentException.class, failure.getCause());
                await(stopped);
                assertTrue(interrupted.get());
            } finally {
                release.countDown();
            }
        }
    }
}
