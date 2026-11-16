package com.fszuberski.multithreading.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(12)
class ExerciseContractsTest {
    @Test
    void shouldDiscoverNestedTemplatesAndEffectiveMetadataGivenContractHierarchy() {
        var cases = ExerciseContracts.testCases(ContractData.class);
        var byName = cases.stream().collect(Collectors.toMap(test -> test.method().getName(), test -> test));
        assertEquals(3, cases.size());
        assertEquals(Map.of("shouldRunGivenRoot", 7L, "shouldRunGivenParameter", 7L,
                        "shouldRunGivenOverride", 2L),
                cases.stream().collect(Collectors.toMap(test -> test.method().getName(),
                        test -> test.timeout().value())));
        cases.forEach(test -> assertTrue(test.tags().contains("fixture")));
        assertTrue(byName.get("shouldRunGivenParameter").tags().contains("nested"));
        assertEquals(TimeUnit.MILLISECONDS, byName.get("shouldRunGivenOverride").timeout().unit());
        assertFalse(cases.stream().anyMatch(test -> test.tags().contains("exercise")));
    }

    @Test
    void shouldExposeMissingMetadataGivenUntaggedUnboundedTest() {
        var cases = ExerciseContracts.testCases(UnboundedData.class);
        assertEquals(1, cases.size());
        assertTrue(cases.getFirst().tags().isEmpty());
        assertNull(cases.getFirst().timeout());
    }

    // Abstract roots: these metadata fixtures must be inspected, never executed.
    @Tag("fixture")
    @Timeout(7)
    abstract static class ContractData {
        @Test
        void shouldRunGivenRoot() { fail("Metadata only"); }

        @Nested
        @Tag("nested")
        class Parameters {
            @ParameterizedTest
            @ValueSource(ints = {1, 2})
            void shouldRunGivenParameter(int value) { fail("Metadata only"); }

            @Test
            @Timeout(value = 2, unit = TimeUnit.MILLISECONDS)
            void shouldRunGivenOverride() { fail("Metadata only"); }

            void helper() {}
        }
    }

    abstract static class UnboundedData {
        @Test
        void shouldRunGivenNoMetadata() { fail("Metadata only"); }
    }
}
