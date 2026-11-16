package com.fszuberski.multithreading.pipelines;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Exercise 13B. Single consumer; producers publish all elements before marking themselves done. */
public final class BatchingProcessor {
    public <T> void run(BlockingQueue<T> queue, int maxBatch, BooleanSupplier producersDone,
                        Consumer<List<T>> sink) throws InterruptedException {
        throw new UnsupportedOperationException("TODO 13B: bounded batching and graceful drain");
    }
}
