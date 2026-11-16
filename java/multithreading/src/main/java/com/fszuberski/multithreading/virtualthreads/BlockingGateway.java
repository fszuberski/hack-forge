package com.fszuberski.multithreading.virtualthreads;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/** Exercise 12B. Replace a fixed pool of blocking request handlers with task-per-virtual-thread. */
public final class BlockingGateway {
    public List<String> fetchAll(List<? extends Callable<String>> requests)
            throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("TODO 12B: preserve blocking code, change execution model");
    }
}
