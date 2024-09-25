package com.fszuberski.bookstore.availability.core.domain;

import java.util.UUID;

public record BookDetails(UUID id, String name, String author) {
    public BookDetails {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be empty");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be empty");
        }

        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("author cannot be empty");
        }
    }
}
