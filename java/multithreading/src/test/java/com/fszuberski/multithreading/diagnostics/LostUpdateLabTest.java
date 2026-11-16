package com.fszuberski.multithreading.diagnostics;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("exercise")
@Timeout(12)
class LostUpdateLabTest {
    @RepeatedTest(20)
    void shouldLoseExactlyOneIncrementGivenControlledInterleaving() throws Exception {
        var result = new LostUpdateLab().reproduce();
        assertEquals(2, result.expected());
        assertEquals(1, result.observed(), "Force the interleaving; do not hope the scheduler picks it");
    }
}
