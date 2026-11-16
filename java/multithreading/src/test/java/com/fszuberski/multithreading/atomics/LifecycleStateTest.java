package com.fszuberski.multithreading.atomics;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.fszuberski.multithreading.atomics.LifecycleState.State.*;
import static com.fszuberski.multithreading.support.TestThreads.race;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class LifecycleStateTest {
    @Test
    void shouldAllowOneWinnerAndReachTerminationGivenConcurrentTransitionAttempts() throws Exception {
        var state = new LifecycleState();
        assertEquals(NEW, state.current());
        assertEquals(1, race(16, () -> state.transition(NEW, RUNNING)).stream().filter(Boolean::booleanValue).count());
        assertFalse(state.transition(NEW, RUNNING));
        assertTrue(state.transition(RUNNING, STOPPING));
        assertTrue(state.transition(STOPPING, TERMINATED));
        assertEquals(TERMINATED, state.current());
    }

    @ParameterizedTest
    @CsvSource({"NEW, TERMINATED", "TERMINATED, NEW", "NEW, NEW"})
    void shouldRejectIllegalEdgeGivenAnyExpectedState(LifecycleState.State from, LifecycleState.State to) {
        var state = new LifecycleState();
        assertThrows(IllegalArgumentException.class, () -> state.transition(from, to));
    }
}
