package com.fszuberski.multithreading.monitors;

import com.fszuberski.multithreading.support.TestThreads;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Nested;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class BoundedBufferTest {
    @Test
    void shouldBlockProducerAndRetainFifoOrderGivenFullBuffer() throws Exception {
        var buffer = new BoundedBuffer<Integer>(1);
        buffer.put(1);
        try (var threads = new TestThreads(1)) {
            var entered = new CountDownLatch(1);
            var producer = threads.submit(() -> {
                entered.countDown();
                buffer.put(2);
                return null;
            });
            await(entered);
            assertThrows(TimeoutException.class, () -> producer.get(100, TimeUnit.MILLISECONDS));
            assertEquals(1, buffer.take());
            result(producer);
            assertEquals(2, buffer.take());
        }
    }

    @Test
    void shouldWakeConsumerGivenPutIntoEmptyBuffer() throws Exception {
        var buffer = new BoundedBuffer<String>(2);
        try (var threads = new TestThreads(1)) {
            var entered = new CountDownLatch(1);
            var consumer = threads.submit(() -> {
                entered.countDown();
                return buffer.take();
            });
            await(entered);
            assertThrows(TimeoutException.class, () -> consumer.get(100, TimeUnit.MILLISECONDS));
            buffer.put("value");
            assertEquals("value", result(consumer));
        }
    }

    @Nested
    class InvalidArguments {
        @Test
        void shouldRejectCapacityGivenZeroCapacity() {
            assertThrows(IllegalArgumentException.class, () -> new BoundedBuffer<>(0));
        }

        @Test
        void shouldRejectElementGivenNullValue() {
            assertThrows(NullPointerException.class, () -> new BoundedBuffer<>(1).put(null));
        }
    }
}
