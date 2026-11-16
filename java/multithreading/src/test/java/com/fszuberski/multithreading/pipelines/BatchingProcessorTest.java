package com.fszuberski.multithreading.pipelines;

import com.fszuberski.multithreading.support.TestThreads;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class BatchingProcessorTest {
    @Test
    void shouldDrainBoundedImmutableBatchesGivenFinalPartialBatch() throws Exception {
        var input = IntStream.range(0, 11).boxed().toList();
        var queue = new ArrayBlockingQueue<Integer>(16);
        queue.addAll(input);
        var output = new ArrayList<Integer>();
        var batches = new ArrayList<List<Integer>>();
        new BatchingProcessor().run(queue, 4, () -> true, batch -> {
            assertTrue(!batch.isEmpty() && batch.size() <= 4);
            batches.add(batch);
            output.addAll(batch);
        });
        assertEquals(input, output);
        for (var batch : batches) {
            assertThrows(UnsupportedOperationException.class, () -> batch.add(99));
        }
    }

    @Test
    void shouldContinueProcessingGivenInitiallyEmptyUnfinishedInput() throws Exception {
        var queue = new ArrayBlockingQueue<Integer>(2);
        var done = new AtomicBoolean();
        var firstHandled = new CountDownLatch(1);
        var output = new ArrayList<Integer>();
        try (var threads = new TestThreads(1)) {
            var worker = threads.submit(() -> {
                new BatchingProcessor().run(queue, 4, done::get, batch -> {
                    output.addAll(batch);
                    firstHandled.countDown();
                });
                return null;
            });
            try {
                queue.put(1);
                await(firstHandled);
                queue.put(2);
            } finally {
                done.set(true);
            }
            result(worker);
        }
        assertEquals(List.of(1, 2), output);
    }
}
