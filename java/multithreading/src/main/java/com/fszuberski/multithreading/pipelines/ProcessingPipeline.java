package com.fszuberski.multithreading.pipelines;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/** Exercise 13A. Producer -> transformer -> collector, with two bounded handoff queues. */
public final class ProcessingPipeline {
    public <I, O> List<O> run(List<I> input, Function<I, O> transform, int capacity)
            throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("TODO 13A: backpressure, end-of-stream, failure propagation");
    }
}
