package com.fszuberski.multithreading.diagnostics;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.CountDownLatch;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class ThreadDiagnosticsTest {
    @Test
    void shouldDistinguishBlockedAndWaitingThreadsGivenContendedMonitor() throws Exception {
        Object monitor = new Object();
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        String ownerName = uniqueName("diagnostics-owner");
        String contenderName = uniqueName("diagnostics-contender");
        Thread owner = Thread.ofPlatform().daemon(true).name(ownerName).unstarted(() -> {
            synchronized (monitor) {
                entered.countDown();
                try {
                    release.await();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        Thread contender = Thread.ofPlatform().daemon(true).name(contenderName).unstarted(() -> {
            synchronized (monitor) {
                // Acquiring the monitor is the only work in this fixture.
            }
        });
        owner.start();
        try {
            await(entered);
            contender.start();
            awaitCondition(() -> contender.getState() == Thread.State.BLOCKED
                    && owner.getState() == Thread.State.WAITING);
            var snapshot = ManagementFactory.getThreadMXBean().getThreadInfo(
                    new long[]{owner.threadId(), contender.threadId()}, true, true);
            var report = new ThreadDiagnostics().inspect(snapshot);
            assertTrue(report.blocked().contains(contenderName));
            assertFalse(report.blocked().contains(ownerName));
            assertTrue(report.waiting().contains(ownerName));
            assertEquals(ownerName, report.lockOwners().get(contenderName));
            assertThrows(UnsupportedOperationException.class, () -> report.blocked().add("other"));
        } finally {
            release.countDown();
            owner.interrupt();
            owner.join(WAIT.toMillis());
            contender.join(WAIT.toMillis());
            assertFalse(owner.isAlive());
            assertFalse(contender.isAlive());
        }
    }

    @Test
    void shouldIgnoreNullEntriesGivenTerminatedThreads() {
        var report = new ThreadDiagnostics().inspect(new ThreadInfo[]{null});
        assertTrue(report.blocked().isEmpty());
        assertTrue(report.waiting().isEmpty());
        assertTrue(report.lockOwners().isEmpty());
    }
}
