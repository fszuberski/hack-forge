package com.fszuberski.multithreading.lifecycle;

import java.time.Duration;

/** Exercise 01A. Own exactly one platform thread; see docs/topics/01-lifecycle.md. */
public final class CancellableWorker {
    @FunctionalInterface
    public interface InterruptibleTask {
        void run() throws InterruptedException;
    }

    private final InterruptibleTask task;

    public CancellableWorker(InterruptibleTask task) {
        this.task = task;
    }

    public void start() {
        throw new UnsupportedOperationException("TODO 01A: start exactly once");
    }

    public void stop() {
        throw new UnsupportedOperationException("TODO 01A: request cancellation, do not join here");
    }

    public boolean awaitStopped(Duration timeout) throws InterruptedException {
        throw new UnsupportedOperationException("TODO 01A: bounded join");
    }
}
