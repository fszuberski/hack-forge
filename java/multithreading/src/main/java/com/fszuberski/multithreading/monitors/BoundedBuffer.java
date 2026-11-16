package com.fszuberski.multithreading.monitors;

/** Exercise 03B. Implement with synchronized/wait/notifyAll, not BlockingQueue. */
public final class BoundedBuffer<E> {
    private final int capacity;

    public BoundedBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    public void put(E element) throws InterruptedException {
        throw new UnsupportedOperationException("TODO 03B: wait while full");
    }

    public E take() throws InterruptedException {
        throw new UnsupportedOperationException("TODO 03B: wait while empty");
    }
}
