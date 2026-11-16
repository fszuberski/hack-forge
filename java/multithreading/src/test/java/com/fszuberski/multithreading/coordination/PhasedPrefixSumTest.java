package com.fszuberski.multithreading.coordination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class PhasedPrefixSumTest {
    @Nested
    class Computation {
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 8})
        void shouldComputeInclusivePrefixWithoutMutationGivenWorkerCount(int workers) throws Exception {
            var input = new int[]{3, -1, 4, 0, 2};
            assertArrayEquals(new long[]{3, 2, 6, 6, 8}, new PhasedPrefixSum().compute(input, workers));
            assertArrayEquals(new int[]{3, -1, 4, 0, 2}, input);
        }

        @ParameterizedTest
        @MethodSource("com.fszuberski.multithreading.coordination.PhasedPrefixSumTest#boundaryInputs")
        void shouldComputePrefixGivenBoundaryInput(int[] input, int workers, long[] expected) throws Exception {
            assertArrayEquals(expected, new PhasedPrefixSum().compute(input, workers));
        }
    }

    static Stream<Arguments> boundaryInputs() {
        return Stream.of(
                Arguments.of(new int[0], 4, new long[0]),
                Arguments.of(new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE}, 2,
                        new long[]{Integer.MAX_VALUE, 2L * Integer.MAX_VALUE}),
                Arguments.of(IntStream.rangeClosed(1, 1_003).toArray(), 7,
                        IntStream.rangeClosed(1, 1_003).mapToLong(n -> (long) n * (n + 1) / 2).toArray()));
    }

    @Test
    void shouldRejectWorkerCountGivenZeroWorkers() {
        assertThrows(IllegalArgumentException.class,
                () -> new PhasedPrefixSum().compute(IntStream.rangeClosed(1, 1_003).toArray(), 0));
    }
}
