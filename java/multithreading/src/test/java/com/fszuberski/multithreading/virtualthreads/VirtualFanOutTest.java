package com.fszuberski.multithreading.virtualthreads;

import com.fszuberski.multithreading.support.TestThreads;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Nested;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class VirtualFanOutTest {
    @Test
    void shouldLimitDownstreamCallsGivenVirtualThreadFanOut() throws Exception {
        var entered = new CountDownLatch(2);
        var release = new CountDownLatch(1);
        var active = new AtomicInteger();
        var maximum = new AtomicInteger();
        List<Callable<Integer>> jobs = IntStream.range(0, 8).mapToObj(i -> (Callable<Integer>) () -> {
            assertTrue(Thread.currentThread().isVirtual());
            int count = active.incrementAndGet();
            maximum.accumulateAndGet(count, Math::max);
            entered.countDown();
            try {
                await(release);
                return i;
            } finally {
                active.decrementAndGet();
            }
        }).toList();
        try (var threads = new TestThreads(1)) {
            var result = threads.submit(() -> new VirtualFanOut().invoke(jobs, 2));
            try {
                await(entered);
                assertEquals(2, maximum.get());
            } finally {
                release.countDown();
            }
            assertEquals(IntStream.range(0, 8).boxed().toList(), result(result));
            assertEquals(2, maximum.get());
        }
    }

    @Nested
    class InputBoundary {
        @Test
        void shouldReturnEmptyResultsGivenNoJobs() throws Exception {
            assertEquals(List.of(), new VirtualFanOut().invoke(List.of(), 1));
        }

        @Test
        void shouldRejectConcurrencyGivenZeroLimit() {
            assertThrows(IllegalArgumentException.class, () -> new VirtualFanOut().invoke(List.of(), 0));
        }
    }

    @Test
    void shouldPropagateCauseGivenFailedJob() {
        List<Callable<Integer>> jobs = List.of(() -> { throw new IllegalArgumentException("failed"); });
        assertInstanceOf(IllegalArgumentException.class,
                assertThrows(ExecutionException.class, () -> new VirtualFanOut().invoke(jobs, 1)).getCause());
    }
}
