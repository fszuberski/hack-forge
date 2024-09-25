package com.fszuberski.bookstore.availability.core.service;

import com.fszuberski.bookstore.availability.core.domain.BookAvailability;
import com.fszuberski.bookstore.availability.port.in.CheckBookAvailabilityUseCase;
import com.fszuberski.bookstore.availability.port.out.GetBookAvailabilityPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class BookAvailabilityService implements CheckBookAvailabilityUseCase {

    private final GetBookAvailabilityPort getBookAvailabilityPort;

    @Override
    public Optional<BookAvailability> checkAvailabilityById(UUID bookId) {
        return getBookAvailabilityPort.getBookAvailabilityById(bookId);
    }
}
