package com.fszuberski.multithreading.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;
import org.junit.platform.commons.annotation.Testable;
import org.junit.platform.commons.support.AnnotationSupport;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The course's exercise boundary, independent of the number/layout of helper tests. */
public final class ExerciseContracts {
    private static final String PACKAGE = "com.fszuberski.multithreading.";
    private static final List<String> CONTRACTS = List.of(
            "lifecycle.CancellableWorkerTest", "lifecycle.CoordinatedStartTest",
            "memory.PublishedConfigTest", "memory.ShutdownSignalTest",
            "monitors.SynchronizedCounterTest", "monitors.BoundedBufferTest",
            "locks.TimedTransfersTest", "locks.CloseableQueueTest",
            "atomics.LifecycleStateTest", "atomics.MetricCountersTest",
            "collections.FrequencyTrackerTest", "collections.AtomicInventoryTest",
            "executors.ParallelMapperTest", "executors.FutureBatchTest",
            "cancellation.InterruptibleProcessorTest", "cancellation.GracefulShutdownTest",
            "coordination.RequestLimiterTest", "coordination.PhasedPrefixSumTest",
            "composition.ServiceAggregatorTest", "composition.TimeoutFallbackTest",
            "parallelism.ParallelSumTest", "parallelism.ParallelGroupingTest",
            "virtualthreads.VirtualFanOutTest", "virtualthreads.BlockingGatewayTest",
            "pipelines.ProcessingPipelineTest", "pipelines.BatchingProcessorTest",
            "caching.SingleFlightTest", "caching.ExpiringCacheTest",
            "contention.DeadlockFreeLedgerTest", "contention.StripedAggregatorTest",
            "diagnostics.LostUpdateLabTest", "diagnostics.ThreadDiagnosticsTest");

    private ExerciseContracts() {}

    public static List<Class<?>> registeredClasses() {
        return CONTRACTS.stream().<Class<?>>map(name -> load(PACKAGE + name)).toList();
    }

    public static List<Class<?>> sourceTestClasses(Path sourceRoot) throws IOException {
        try (var paths = Files.walk(sourceRoot)) {
            return paths.filter(path -> path.toString().endsWith("Test.java"))
                    .map(sourceRoot::relativize)
                    .map(Path::toString)
                    .map(name -> name.substring(0, name.length() - ".java".length())
                            .replace(java.io.File.separatorChar, '.'))
                    .map(ExerciseContracts::load).toList();
        }
    }

    private static Class<?> load(String name) {
        try {
            return Class.forName(name, false, ExerciseContracts.class.getClassLoader());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Missing compiled test contract: " + name, exception);
        }
    }

    public static List<CaseData> testCases(Class<?> testClass) {
        var cases = new ArrayList<CaseData>();
        collect(testClass, Set.of(), null, cases);
        return List.copyOf(cases);
    }

    private static void collect(Class<?> type, Set<String> enclosingTags, Timeout enclosingTimeout,
                                List<CaseData> cases) {
        var tags = new HashSet<>(enclosingTags);
        AnnotationSupport.findRepeatableAnnotations(type, Tag.class).forEach(tag -> tags.add(tag.value()));
        var timeout = AnnotationSupport.findAnnotation(type, Timeout.class).orElse(enclosingTimeout);
        for (var method : type.getDeclaredMethods()) {
            // Includes @Test, @ParameterizedTest and @RepeatedTest via their meta-annotations.
            if (AnnotationSupport.isAnnotated(method, Testable.class)) {
                var methodTags = new HashSet<>(tags);
                AnnotationSupport.findRepeatableAnnotations(method, Tag.class)
                        .forEach(tag -> methodTags.add(tag.value()));
                cases.add(new CaseData(method, Set.copyOf(methodTags),
                        AnnotationSupport.findAnnotation(method, Timeout.class).orElse(timeout)));
            }
        }
        for (var nested : type.getDeclaredClasses()) {
            if (AnnotationSupport.isAnnotated(nested, Nested.class)) {
                collect(nested, tags, timeout, cases);
            }
        }
    }

    public record CaseData(Method method, Set<String> tags, Timeout timeout) {}
}
