package com.fszuberski.wc.adapter.cli;

import com.fszuberski.wc.adapter.cli.exception.FilePathNotProvidedException;
import com.fszuberski.wc.adapter.cli.exception.InvalidFlagException;
import com.fszuberski.wc.adapter.cli.exception.TooManyArgumentsException;
import com.fszuberski.wc.application.domain.CountResult;
import com.fszuberski.wc.application.port.in.CountUseCase;
import com.fszuberski.wc.application.service.BufferedInputSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static com.fszuberski.wc.application.domain.CountType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommandLineAdapterTest {

    private CountUseCase countUseCaseMock;
    private CommandLineAdapter commandLineAdapter;

    private final PrintStream standardOut = System.out;
    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeEach
    public void beforeEach() {
        this.countUseCaseMock = mock(CountUseCase.class);
        this.commandLineAdapter = new CommandLineAdapter(countUseCaseMock);

        this.outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void afterEach() {
        System.setOut(standardOut);
    }

    @Test
    public void throwsExceptionGivenNoArguments() {
        assertThrows(FilePathNotProvidedException.class,
                () -> commandLineAdapter.readInputAndCount(new String[]{}));
    }

    @Test
    public void throwsExceptionGivenFilePathNotProvided() {
        assertThrows(FilePathNotProvidedException.class,
                () -> commandLineAdapter.readInputAndCount(new String[]{"-lwc"}));
    }

    @Test
    public void throwsExceptionGivenTooManyArgumentsProvided() {
        assertThrows(TooManyArgumentsException.class,
                () -> commandLineAdapter.readInputAndCount(new String[]{"-lwc", "test.txt", "test2.txt"}));
    }

    @Test
    public void throwsExceptionGivenInvalidFlagProvided() {
        assertThrows(InvalidFlagException.class,
                () -> commandLineAdapter.readInputAndCount(new String[]{"-lwcv", "test.txt"}));
    }

    @ParameterizedTest
    @MethodSource("printsResultsArguments")
    public void printsResults(final String[] flags, final CountResult expectedCountResult) throws IOException {
        when(countUseCaseMock.count(any(BufferedInputSupplier.class), any())).thenReturn(expectedCountResult);
        var testFileResource = this.getClass().getClassLoader().getResource("test.txt");

        var arguments = Stream
                .concat(Arrays.stream(flags), Stream.of(testFileResource.getPath()))
                .toList()
                .toArray(String[]::new);
        commandLineAdapter.readInputAndCount(arguments);

        assertTrue(outputStreamCaptor.toString().contains(expectedCountResult.toString()));
    }

    private static Stream<Arguments> printsResultsArguments() {
        return Stream.of(
                Arguments.of(new String[]{"-c"},
                        CountResult.of(Map.of(BYTES, 1L))),
                Arguments.of(new String[]{"-l"},
                        CountResult.of(Map.of(LINES, 2L))),
                Arguments.of(new String[]{"-w"},
                        CountResult.of(Map.of(CHARACTERS, 3L))),
                Arguments.of(new String[]{"-m"},
                        CountResult.of(Map.of(WORDS, 4L))),
                Arguments.of(new String[]{"-lwcm"},
                        CountResult.of(Map.of(BYTES, 1L, LINES, 2L, CHARACTERS, 3L, WORDS, 4L))),
                Arguments.of(new String[]{"-l", "-w", "-c", "-m"},
                        CountResult.of(Map.of(BYTES, 1L, LINES, 2L, CHARACTERS, 3L, WORDS, 4L)))
        );
    }
}