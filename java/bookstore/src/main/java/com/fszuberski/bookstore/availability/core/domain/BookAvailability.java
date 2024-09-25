package com.fszuberski.bookstore.availability.core.domain;

public record BookAvailability(BookDetails bookDetails, int availableStock) {
    public BookAvailability {
        if (bookDetails == null) {
            throw new IllegalArgumentException("book details cannot be empty");
        }

        if (availableStock < 0) {
            throw new IllegalArgumentException("available stock cannot be less than zero");
        }
    }
}
