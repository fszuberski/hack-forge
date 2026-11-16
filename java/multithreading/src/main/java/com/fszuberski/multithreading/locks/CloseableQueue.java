package com.fszuberski.multithreading.locks;

import java.util.Optional;

/** Exercise 04B. Use ReentrantLock and separate notEmpty/notFull conditions. */
public final class CloseableQueue<E> implements AutoCloseable {
    private final int capacity;

    public CloseableQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    public void put(E value) throws InterruptedException {
        throw new UnsupportedOperationException("TODO 04B: block while full, reject after close");
    }

    public Optional<E> take() throws InterruptedException {
        throw new UnsupportedOperationException("TODO 04B: drain before reporting closed");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("TODO 04B: wake every producer and consumer");
    }
}
