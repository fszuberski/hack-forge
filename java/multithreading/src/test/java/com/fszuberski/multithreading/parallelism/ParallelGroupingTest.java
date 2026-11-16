package com.fszuberski.multithreading.parallelism;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("exercise")
@Timeout(12)
class ParallelGroupingTest {
    @Test
    void shouldCountRepeatedKeysGivenConcurrentGrouping() {
        var grouping = new ParallelGrouping();
        var input = IntStream.range(0, 30_000).mapToObj(i -> "key-" + i % 3).toList();
        // Exercise reuse of the same instance, including the transition to empty input.
        for (int repetition = 0; repetition < 5; repetition++) {
            assertEquals(Map.of("key-0", 10_000L, "key-1", 10_000L, "key-2", 10_000L), grouping.count(input));
        }
        assertEquals(Map.of(), grouping.count(List.of()));
    }
}
