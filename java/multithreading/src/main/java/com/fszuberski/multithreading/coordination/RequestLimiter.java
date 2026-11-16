package com.fszuberski.multithreading.coordination;

import java.util.concurrent.Callable;

/** Exercise 09A. A semaphore limits scarce resources, independent of thread count. */
public final class RequestLimiter {
    public RequestLimiter(int permits) {
        if (permits <= 0) {
            throw new IllegalArgumentException("permits must be positive");
        }
        // TODO 09A: initialize semaphore.
    }

    public <T> T call(Callable<T> action) throws Exception {
        throw new UnsupportedOperationException("TODO 09A: acquire interruptibly and release in finally");
    }

    public int availablePermits() {
        throw new UnsupportedOperationException("TODO 09A: diagnostic permit count");
    }
}
