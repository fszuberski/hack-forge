package com.fszuberski.multithreading.executors;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/** Exercise 07B. Caller owns the executor; this operation owns cancellation of the supplied futures. */
public final class FutureBatch {
    public <T> List<T> collect(List<? extends Future<T>> futures, Duration timeout)
            throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("TODO 07B: deadline, failure, cancellation");
    }
}
