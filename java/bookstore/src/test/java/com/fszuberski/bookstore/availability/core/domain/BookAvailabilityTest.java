package com.fszuberski.bookstore.availability.core.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookAvailabilityTest {


    @Test
    public void shouldThrowExceptionGivenBookDetailsAreNull() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookAvailability(null, 0)
        );

        assertEquals("book details cannot be empty", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionGivenAvailableStockIsLessThan0() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookAvailability(
                        new BookDetails(UUID.randomUUID(), "Test Name", "Test Author"),
                        -1
                )
        );

        assertEquals("available stock cannot be less than zero", exception.getMessage());
    }

    @Test
    public void shouldInstantiateBookAvailabilityGivenValidConstructorParameters() {
        var expectedBookDetails = new BookDetails(UUID.randomUUID(), "Test Name", "Test Author");
        var expectedAvailableStock = 10;

        var result = new BookAvailability(expectedBookDetails, expectedAvailableStock);

        assertNotNull(result);
        assertEquals(expectedBookDetails, result.bookDetails());
        assertEquals(expectedAvailableStock, result.availableStock());
    }
}