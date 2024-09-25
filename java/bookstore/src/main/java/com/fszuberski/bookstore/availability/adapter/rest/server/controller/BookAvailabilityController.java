package com.fszuberski.bookstore.availability.adapter.rest.server.controller;

import com.fszuberski.bookstore.availability.core.domain.BookAvailability;
import com.fszuberski.bookstore.availability.port.in.CheckBookAvailabilityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/book")
public class BookAvailabilityController {
    private final CheckBookAvailabilityUseCase checkBookAvailabilityUseCase;

    @GetMapping(path = "/{bookId}/availability")
    public ResponseEntity<BookAvailability> getBookAvailability(@PathVariable String bookId) {
        UUID bookUUID;

        try {
            bookUUID = UUID.fromString(bookId);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("invalid uuid: '%s'", bookId));
        }

        Optional<BookAvailability> bookAvailability;

        try {
            bookAvailability = checkBookAvailabilityUseCase.checkAvailabilityById(bookUUID);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("invalid request: %s", e.getMessage())
            );
        }

        if (bookAvailability.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("book with id='%s' does not exist", bookId));
        }

        return ResponseEntity.of(bookAvailability);
    }

}
