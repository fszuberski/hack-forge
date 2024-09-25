package com.fszuberski.bookstore.availability.adapter.rest.server.model;

public record ErrorResponse(String message) {
    public static ErrorResponse of(String message) {
        return new ErrorResponse(message);
    }
}
