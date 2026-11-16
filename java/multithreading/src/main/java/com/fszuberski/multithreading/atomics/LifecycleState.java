package com.fszuberski.multithreading.atomics;

/** Exercise 05A. Linearizable state transitions using AtomicReference and CAS. */
public final class LifecycleState {
    public enum State { NEW, RUNNING, STOPPING, TERMINATED }

    public State current() {
        throw new UnsupportedOperationException("TODO 05A: read state");
    }

    /** Only NEW->RUNNING->STOPPING->TERMINATED edges are legal. */
    public boolean transition(State expected, State next) {
        throw new UnsupportedOperationException("TODO 05A: validate edge and compare-and-set");
    }
}
