package com.fszuberski.bookstore.availability.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookDetailsTest {

    @Test
    public void shouldThrowExceptionGivenIdIsNull() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookDetails(null, "Test Name", "Test Author")
        );

        assertEquals("id cannot be empty", exception.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    public void shouldThrowExceptionGivenNameIsInvalid(String bookName) {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookDetails(UUID.randomUUID(), bookName, "Test Author")
        );

        assertEquals("name cannot be empty", exception.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    public void shouldThrowExceptionGivenAuthorIsInvalid(String author) {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookDetails(UUID.randomUUID(), "Test Book", author)
        );

        assertEquals("author cannot be empty", exception.getMessage());
    }

    @Test
    public void shouldInstantiateBookDetailsGivenValidNameAndAuthor() {
        var expectedName = "Test Book";
        var expectedAuthor = "Test Author";

        var result = new BookDetails(UUID.randomUUID(), expectedName, expectedAuthor);

        assertNotNull(result);
        assertEquals(expectedName, result.name());
        assertEquals(expectedAuthor, result.author());
    }
}