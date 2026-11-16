package com.fszuberski.multithreading.pipelines;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class ProcessingPipelineTest {
    @ParameterizedTest
    @MethodSource("inputs")
    void shouldPreserveAllDataAndOrderGivenSmallQueuesAndInput(List<Integer> input) throws Exception {
        var expected = input.stream().map(i -> (long) i * i).toList();
        assertEquals(expected, new ProcessingPipeline().run(input, i -> (long) i * i, 1));
    }

    static Stream<List<Integer>> inputs() {
        return Stream.of(IntStream.rangeClosed(-100, 100).boxed().toList(), List.of());
    }

    @Test
    void shouldPropagateFailureWithoutStuckStagesGivenThrowingTransformer() {
        var input = IntStream.range(0, 1_000).boxed().toList();
        var failure = assertThrows(ExecutionException.class, () -> new ProcessingPipeline().run(input, value -> {
            if (value == 3) {
                throw new IllegalArgumentException("bad message");
            }
            return value;
        }, 1));
        assertInstanceOf(IllegalArgumentException.class, failure.getCause());
    }

    @Test
    void shouldRejectQueueCapacityGivenZeroCapacity() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProcessingPipeline().run(List.of(), value -> value, 0));
    }
}
