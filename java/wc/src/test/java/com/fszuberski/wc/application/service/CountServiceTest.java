package com.fszuberski.wc.application.service;

import com.fszuberski.wc.application.domain.CountResult;
import com.fszuberski.wc.application.domain.CountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CountServiceTest {

    private CountService countService;

    @BeforeEach
    public void beforeEach() {
        this.countService = new CountService();
    }

    @Test
    public void shouldReturnEmptyCountResultGivenNullBufferedInputSupplier() {
        var result = countService.count(null, Set.of(CountType.WORDS));

        assertNotNull(result);
        assertEquals(0, result.resultsPerType().size());
    }

    @Test
    public void shouldReturnEmptyCountResultGivenNullCountTypes() {
        Supplier<String> supplier = () -> "test";
        var result = countService.count(BufferedInputSupplier.from(supplier), null);

        assertNotNull(result);
        assertEquals(0, result.resultsPerType().size());
    }

    @ParameterizedTest
    @MethodSource("countParameters")
    public void shouldReturnValidByteCount(final List<String> lines, final CountResult expectedResult) {
        var result = resultForCountTypes(lines, Set.of(CountType.BYTES));

        assertNotNull(result);
        assertEquals(1L, result.resultsPerType().size());
        assertEquals(
                expectedResult.resultsPerType().get(CountType.BYTES),
                result.resultsPerType().get(CountType.BYTES));
    }

    @ParameterizedTest
    @MethodSource("countParameters")
    public void shouldReturnValidLineCount(final List<String> lines, final CountResult expectedResult) {
        var result = resultForCountTypes(lines, Set.of(CountType.LINES));

        assertNotNull(result);
        assertEquals(1L, result.resultsPerType().size());
        assertEquals(
                expectedResult.resultsPerType().get(CountType.LINES),
                result.resultsPerType().get(CountType.LINES));
    }

    @ParameterizedTest
    @MethodSource("countParameters")
    public void shouldReturnValidCharactersCount(final List<String> lines, final CountResult expectedResult) {
        var result = resultForCountTypes(lines, Set.of(CountType.CHARACTERS));

        assertNotNull(result);
        assertEquals(1L, result.resultsPerType().size());
        assertEquals(
                expectedResult.resultsPerType().get(CountType.CHARACTERS),
                result.resultsPerType().get(CountType.CHARACTERS));
    }

    @ParameterizedTest
    @MethodSource("countParameters")
    public void shouldReturnValidWordCount(final List<String> lines, final CountResult expectedResult) {
        var result = resultForCountTypes(lines, Set.of(CountType.WORDS));

        assertNotNull(result);
        assertEquals(1L, result.resultsPerType().size());
        assertEquals(
                expectedResult.resultsPerType().get(CountType.WORDS),
                result.resultsPerType().get(CountType.WORDS));
    }

    @ParameterizedTest
    @MethodSource("countParameters")
    public void shouldReturnValidCountForAllTypes(final List<String> lines, final CountResult expectedResult) {
        var allTypes = Set.of(CountType.BYTES, CountType.LINES, CountType.CHARACTERS, CountType.WORDS);
        var result = resultForCountTypes(lines, allTypes);

        assertNotNull(result);
        assertEquals(allTypes.size(), result.resultsPerType().size());
        allTypes.forEach(type -> {
            assertEquals(
                    expectedResult.resultsPerType().get(type),
                    result.resultsPerType().get(type));
        });
    }

    private CountResult resultForCountTypes(final List<String> lines, final Set<CountType> countTypes) {
        var iterator = lines.iterator();
        Supplier<String> supplier = () -> iterator.hasNext() ? iterator.next() : null;
        return countService.count(BufferedInputSupplier.from(supplier), countTypes);
    }

    private static Stream<Arguments> countParameters() {
        return Stream.of(
                Arguments.of(Arrays.asList(
                                "",
                                null),
                        CountResult.of(Map.of(
                                CountType.BYTES, 0L,
                                CountType.LINES, 1L,
                                CountType.CHARACTERS, 0L,
                                CountType.WORDS, 0L
                        ))),
                Arguments.of(Arrays.asList(
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore",
                                null),
                        CountResult.of(Map.of(
                                CountType.BYTES, 109L,
                                CountType.LINES, 1L,
                                CountType.CHARACTERS, 109L,
                                CountType.WORDS, 17L
                        ))),
                Arguments.of(Arrays.asList(
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore",
                                "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                                null),
                        CountResult.of(Map.of(
                                CountType.BYTES, 230L,
                                CountType.LINES, 2L,
                                CountType.CHARACTERS, 230L,
                                CountType.WORDS, 36L
                        ))),
                Arguments.of(Arrays.asList(
                                "L0rem ipsum @dolor ^sit amet\n, consectetur adipiscing* elit. \tSed do \uD83D\uDCA9 eiusmod tempor &incididunt ut labore et dolore",
                                null),
                        CountResult.of(Map.of(
                                CountType.BYTES, 120L,
                                CountType.LINES, 2L,
                                CountType.CHARACTERS, 118L,
                                CountType.WORDS, 19L
                        ))),
                Arguments.of(Arrays.asList(
                                "L0rem ipsum @dolor ^sit amet\n, consectetur adipiscing* elit. \tSed do \uD83D\uDCA9 eiusmod tempor &incididunt ut labore et dolore",
                                "magna aliqua. Ut enim ad minim veniam,\uD83E\uDD21 quis nostrud exercit%ation *&ullamco laboris nisi 123ut aliquip ex ea commodo consequat.\n",
                                null),
                        CountResult.of(Map.of(
                                CountType.BYTES, 252L,
                                CountType.LINES, 4L,
                                CountType.CHARACTERS, 248L,
                                CountType.WORDS, 38L
                        )))
        );
    }
}