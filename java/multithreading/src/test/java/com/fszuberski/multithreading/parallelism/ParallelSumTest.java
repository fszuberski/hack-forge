package com.fszuberski.multithreading.parallelism;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class ParallelSumTest {
    @ParameterizedTest
    @MethodSource("inputs")
    void shouldSumUsingLongsWithoutClosingPoolGivenInput(int[] input, int threshold, long expected)
            throws InterruptedException {
        var pool = new ForkJoinPool(4);
        try {
            assertEquals(expected, new ParallelSum().sum(input, threshold, pool));
            assertFalse(pool.isShutdown());
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(3, TimeUnit.SECONDS));
        }
    }

    static Stream<Arguments> inputs() {
        int[] uneven = IntStream.rangeClosed(-500, 1_003).toArray();
        return Stream.of(Arguments.of(uneven, 17, IntStream.of(uneven).asLongStream().sum()),
                Arguments.of(new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE}, 1, 2L * Integer.MAX_VALUE),
                Arguments.of(new int[0], 1, 0L));
    }

    @Test
    void shouldRejectThresholdGivenZeroThreshold() throws InterruptedException {
        var pool = new ForkJoinPool(4);
        try {
            assertThrows(IllegalArgumentException.class, () -> new ParallelSum().sum(
                    IntStream.rangeClosed(-500, 1_003).toArray(), 0, pool));
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(3, TimeUnit.SECONDS));
        }
    }
}
