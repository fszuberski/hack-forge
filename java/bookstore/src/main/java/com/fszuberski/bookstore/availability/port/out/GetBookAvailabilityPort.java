package com.fszuberski.bookstore.availability.port.out;

import com.fszuberski.bookstore.availability.core.domain.BookAvailability;

import java.util.Optional;
import java.util.UUID;

public interface GetBookAvailabilityPort {
    Optional<BookAvailability> getBookAvailabilityById(UUID id);
}
