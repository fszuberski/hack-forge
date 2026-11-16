package com.fszuberski.multithreading.diagnostics;

/** Exercise 16A. Deliberately force a lost update with gates, then join both threads. */
public final class LostUpdateLab {
    public record Result(int observed, int expected) {}

    public Result reproduce() throws InterruptedException {
        throw new UnsupportedOperationException("TODO 16A: both reads before either write, no sleeps");
    }
}
