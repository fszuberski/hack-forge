package com.fszuberski.multithreading.cancellation;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

/** Exercise 08B. Two-phase termination with explicit ownership transfer from the caller. */
public final class GracefulShutdown {
    public boolean shutdown(ExecutorService executor, Duration grace, Duration forcedWait)
            throws InterruptedException {
        throw new UnsupportedOperationException("TODO 08B: orderly shutdown, then cancellation");
    }
}
