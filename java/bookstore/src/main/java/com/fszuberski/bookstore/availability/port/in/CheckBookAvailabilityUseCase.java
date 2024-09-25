package com.fszuberski.bookstore.availability.port.in;

import com.fszuberski.bookstore.availability.core.domain.BookAvailability;

import java.util.Optional;
import java.util.UUID;

public interface CheckBookAvailabilityUseCase {
    Optional<BookAvailability> checkAvailabilityById(UUID bookId);
}
