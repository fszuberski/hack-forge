package com.fszuberski.multithreading.executors;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/** Exercise 07A. Bound submitted-but-uncollected work and preserve input order. */
public final class ParallelMapper {
    public <T, R> List<R> map(List<T> input, Function<T, R> mapper, int parallelism)
            throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("TODO 07A: bounded scheduling and owned executor cleanup");
    }
}
