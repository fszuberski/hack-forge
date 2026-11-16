package com.fszuberski.multithreading.support;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Bounded test orchestration, not a production concurrency abstraction. */
public final class TestThreads implements AutoCloseable {
    public static final Duration WAIT = Duration.ofSeconds(3);
    private static final AtomicInteger IDS = new AtomicInteger();
    private final ExecutorService executor;
    private final ConcurrentLinkedQueue<Thread> workers = new ConcurrentLinkedQueue<>();

    public TestThreads(int size) {
        var factory = daemonFactory("worker");
        executor = Executors.newFixedThreadPool(size, task -> {
            var worker = factory.newThread(task);
            workers.add(worker);
            return worker;
        });
    }

    public static String uniqueName(String role) {
        return "course-test-" + role + "-" + IDS.incrementAndGet();
    }

    public static ThreadFactory daemonFactory(String role) {
        return task -> Thread.ofPlatform().daemon(true).name(uniqueName(role)).unstarted(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public static <T> T result(Future<T> future) throws Exception {
        return future.get(WAIT.toMillis(), TimeUnit.MILLISECONDS);
    }

    public static void await(CountDownLatch latch) throws InterruptedException {
        assertTrue(latch.await(WAIT.toMillis(), TimeUnit.MILLISECONDS), "Gate did not open");
    }

    public static void awaitCondition(BooleanSupplier condition) {
        long deadline = System.nanoTime() + WAIT.toNanos();
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            assertTrue(!Thread.currentThread().isInterrupted(), "Condition wait interrupted");
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
        assertTrue(condition.getAsBoolean(), "Condition was not reached before the deadline");
    }

    public static <T> List<T> race(int participants, Callable<T> action) throws Exception {
        return raceIndexed(participants, ignored -> action);
    }

    /** Supplies logical indices independent of JVM thread identities or scheduling. */
    public static <T> List<T> raceIndexed(int participants, IntFunction<Callable<T>> actions) throws Exception {
        try (var threads = new TestThreads(participants)) {
            var ready = new CountDownLatch(participants);
            var start = new CountDownLatch(1);
            List<Future<T>> futures = new ArrayList<>();
            for (int i = 0; i < participants; i++) {
                var action = actions.apply(i);
                futures.add(threads.submit(() -> {
                    ready.countDown();
                    await(start);
                    return action.call();
                }));
            }
            try {
                await(ready);
            } finally {
                start.countDown();
            }
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(result(future));
            }
            return results;
        }
    }

    @Override
    public void close() throws InterruptedException {
        executor.shutdownNow();
        long deadline = System.nanoTime() + WAIT.toNanos();
        boolean interrupted = Thread.interrupted();
        try {
            boolean terminated = false;
            while (!terminated && System.nanoTime() < deadline) {
                try {
                    terminated = executor.awaitTermination(Math.max(0, deadline - System.nanoTime()),
                            TimeUnit.NANOSECONDS);
                } catch (InterruptedException expected) {
                    interrupted = true;
                }
            }
            assertTrue(terminated, "Test worker leaked: cancellation must be cooperative");
            // Executor termination can be signalled just before its final worker actually exits.
            for (var worker : workers) {
                while (worker.isAlive() && System.nanoTime() < deadline) {
                    try {
                        worker.join(Duration.ofNanos(Math.max(0, deadline - System.nanoTime())));
                    } catch (InterruptedException expected) {
                        interrupted = true;
                    }
                }
                assertTrue(!worker.isAlive(), "Test worker leaked: " + worker.getName());
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        if (interrupted) {
            throw new InterruptedException("Interrupted while closing test workers");
        }
    }
}
