package com.fszuberski.urlpool;

import java.util.function.IntSupplier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToolingSmokeTest {
    @Test
    void shouldReturnStubbedValueGivenMockitoOnTheTestClasspath() {
        // given: a supplier stubbed with a known value
        var supplier = mock(IntSupplier.class);
        when(supplier.getAsInt()).thenReturn(42);

        // when: the test invokes the mock
        int value = supplier.getAsInt();

        // then: JUnit observes the value configured through Mockito
        assertEquals(42, value);
    }
}
