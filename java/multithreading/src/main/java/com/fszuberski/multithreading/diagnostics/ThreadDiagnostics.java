package com.fszuberski.multithreading.diagnostics;

import java.lang.management.ThreadInfo;
import java.util.List;
import java.util.Map;

/** Exercise 16B. Classify a JVM snapshot; BLOCKED alone does not imply deadlock. */
public final class ThreadDiagnostics {
    public record Report(List<String> blocked, List<String> waiting, Map<String, String> lockOwners) {}

    public Report inspect(ThreadInfo[] snapshot) {
        throw new UnsupportedOperationException("TODO 16B: thread states and lock-owner relationships");
    }
}
