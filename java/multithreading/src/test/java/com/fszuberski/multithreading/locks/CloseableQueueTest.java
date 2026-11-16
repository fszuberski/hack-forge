package com.fszuberski.multithreading.locks;

import com.fszuberski.multithreading.support.TestThreads;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class CloseableQueueTest {
    @Test
    void shouldDrainExistingElementsAndRejectNewOnesGivenClosedQueue() throws Exception {
        var queue = new CloseableQueue<Integer>(2);
        queue.put(1);
        queue.put(2);
        queue.close();
        queue.close();
        assertEquals(Optional.of(1), queue.take());
        assertEquals(Optional.of(2), queue.take());
        assertEquals(Optional.empty(), queue.take());
        assertThrows(IllegalStateException.class, () -> queue.put(3));
    }

    @Test
    void shouldWakeConsumerGivenClosedEmptyQueue() throws Exception {
        var queue = new CloseableQueue<String>(1);
        try (var threads = new TestThreads(1)) {
            var entered = new CountDownLatch(1);
            var consumer = threads.submit(() -> {
                entered.countDown();
                return queue.take();
            });
            try {
                await(entered);
                assertThrows(TimeoutException.class, () -> consumer.get(100, TimeUnit.MILLISECONDS));
            } finally {
                queue.close();
            }
            assertEquals(Optional.empty(), result(consumer));
        }
    }

    @Test
    void shouldWakeProducerGivenClosedFullQueue() throws Exception {
        var queue = new CloseableQueue<Integer>(1);
        queue.put(1);
        try (var threads = new TestThreads(1)) {
            var entered = new CountDownLatch(1);
            var producer = threads.submit(() -> {
                entered.countDown();
                queue.put(2);
                return null;
            });
            try {
                await(entered);
                assertThrows(TimeoutException.class, () -> producer.get(100, TimeUnit.MILLISECONDS));
            } finally {
                queue.close();
            }
            var failure = assertThrows(ExecutionException.class, () -> result(producer));
            assertInstanceOf(IllegalStateException.class, failure.getCause());
            assertEquals(Optional.of(1), queue.take());
        }
    }
}
