package com.fszuberski.multithreading.memory;

/** Exercise 02B. A one-way, non-blocking signal; no locks or polling loop in this class. */
public final class ShutdownSignal {
    public void request() {
        throw new UnsupportedOperationException("TODO 02B: publish shutdown request");
    }

    public boolean isRequested() {
        throw new UnsupportedOperationException("TODO 02B: visibility, not compound atomicity");
    }
}
