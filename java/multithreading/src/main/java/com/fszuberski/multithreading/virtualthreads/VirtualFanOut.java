package com.fszuberski.multithreading.virtualthreads;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/** Exercise 12A. One virtual thread per job; independently bound active downstream calls. */
public final class VirtualFanOut {
    public <T> List<T> invoke(List<? extends Callable<T>> jobs, int maxConcurrent)
            throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("TODO 12A: virtual executor plus resource limiter");
    }
}
