package com.fszuberski.urlshortner.common;

import com.fszuberski.urlshortner.shortening.exception.KeyCollisionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.IllegalFormatCodePointException;
import java.util.stream.Stream;

import static com.fszuberski.urlshortner.common.Validation.isValidUrl;
import static com.fszuberski.urlshortner.common.Validation.validate;
import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    @Test
    public void validateShouldNotThrowExceptionGivenExpressionEvaluatesToTrue() {
        validate(true);
        validate(true, RuntimeException::new);
    }

    @Test
    public void validateShouldThrowIllegalArgumentExceptionGivenExpressionEvaluatesToFalse() {
        assertThrows(IllegalArgumentException.class, () -> validate(false));
    }

    @ParameterizedTest
    @MethodSource("exampleRuntimeExceptions")
    public <T extends RuntimeException> void validateShouldThrowPassedExceptionGivenExpressionEvaluatesToFalse(T exception) {
        assertThrows(exception.getClass(), () -> validate(false, () -> exception));
    }

    @ParameterizedTest
    @MethodSource("exampleValidUrls")
    public void isValidUrlShouldReturnTrueGivenValidUrl(String uri) {
        var result = isValidUrl(uri);
        assertTrue(result);
    }
    @ParameterizedTest
    @MethodSource("exampleInvalidUrls")
    public void isValidUrlShouldReturnFalseGivenInvalidUrl(String uri) {
        var result = isValidUrl(uri);
        assertFalse(result);
    }

    public static Stream<Arguments> exampleRuntimeExceptions() {
        return Stream.of(
                Arguments.of(new RuntimeException()),
                Arguments.of(new IllegalArgumentException()),
                Arguments.of(new IllegalFormatCodePointException(0)),
                Arguments.of(new ArrayIndexOutOfBoundsException()),
                Arguments.of(new KeyCollisionException("key"))
        );
    }

    public static Stream<Arguments> exampleValidUrls() {
        //noinspection HttpUrlsUsage
        return Stream.of(
                Arguments.of("http://example.com"),
                Arguments.of("https://example.com"),
                Arguments.of("https://example.com?query=test"),
                Arguments.of("https://example.com?query=test#First")
        );
    }
    public static Stream<Arguments> exampleInvalidUrls() {
        return Stream.of(
                Arguments.of("example.com"),
                Arguments.of("https[example].com"),
                Arguments.of("https example.com"),
                Arguments.of("https@example.com")
        );
    }

}