package com.fszuberski.wc;

import com.fszuberski.wc.application.domain.CountResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.stream.Stream;

import static com.fszuberski.wc.application.domain.CountType.*;
import static com.fszuberski.wc.application.domain.CountType.WORDS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordCountApplicationIntegrationTest {

    private final PrintStream standardOut = System.out;
    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeEach
    public void beforeEach() {
        this.outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void afterEach() {
        System.setOut(standardOut);
    }

    @Test
    public void throwsExceptionGivenInvalidArguments() {
        assertThrows(RuntimeException.class, () -> WordCountApplication.main(new String[]{}));
    }

    @Test
    public void printsResult() throws IOException {
        var testFileResource = this.getClass().getClassLoader().getResource("test.txt");
        var expectedCountResult = CountResult.of(Map.of(BYTES, 327900L, LINES, 7145L, CHARACTERS, 325002L, WORDS, 58164L));

        WordCountApplication.main(new String[]{"-lcwm", testFileResource.getPath()});
        Stream.of(BYTES, LINES, CHARACTERS, WORDS)
                .forEach(type ->
                        assertTrue(outputStreamCaptor.toString().contains(expectedCountResult.resultsPerType().get(type).toString()))
                );
    }

}