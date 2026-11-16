package com.fszuberski.multithreading.memory;

/** Exercise 02A. Publish whole immutable snapshots, never individual fields. */
public final class PublishedConfig {
    public record Config(String endpoint, int maxConnections) {}

    public void publish(String endpoint, int maxConnections) {
        throw new UnsupportedOperationException("TODO 02A: safe publication");
    }

    /** Returns null until the first publication. */
    public Config current() {
        throw new UnsupportedOperationException("TODO 02A: acquire the published snapshot");
    }
}
