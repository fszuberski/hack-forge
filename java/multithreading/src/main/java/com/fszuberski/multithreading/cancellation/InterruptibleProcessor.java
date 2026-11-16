package com.fszuberski.multithreading.cancellation;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

/** Exercise 08A. Does not own its caller's thread or the input queue. */
public final class InterruptibleProcessor {
    public void process(BlockingQueue<String> input, Consumer<String> handler) throws InterruptedException {
        throw new UnsupportedOperationException("TODO 08A: take and handle until interrupted");
    }
}
