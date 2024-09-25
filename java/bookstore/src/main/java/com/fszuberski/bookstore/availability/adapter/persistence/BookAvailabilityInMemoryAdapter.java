package com.fszuberski.bookstore.availability.adapter.persistence;

import com.fszuberski.bookstore.availability.core.domain.BookAvailability;
import com.fszuberski.bookstore.availability.core.domain.BookDetails;
import com.fszuberski.bookstore.availability.port.out.GetBookAvailabilityPort;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BookAvailabilityInMemoryAdapter implements GetBookAvailabilityPort {
    private final ConcurrentMap<UUID, BookAvailability> availabilityMap;
    private final boolean withTestFailures;

    public BookAvailabilityInMemoryAdapter(boolean withTestData, boolean withTestFailures) {
        if (withTestData) {
            availabilityMap = availabilityMapWithTestData();
        } else {
            availabilityMap = new ConcurrentHashMap<>();
        }

        this.withTestFailures = withTestFailures;
    }

    @Override
    public Optional<BookAvailability> getBookAvailabilityById(UUID id) {
        if (withTestFailures && ThreadLocalRandom.current().nextBoolean()) {
            throw new RuntimeException("failure");
        }
        return Optional.ofNullable(availabilityMap.get(id));
    }

    private ConcurrentMap<UUID, BookAvailability> availabilityMapWithTestData() {
        return IntStream.range(0, 10)
                .mapToObj(i -> UUID.randomUUID())
                .map(uuid -> new BookDetails(
                        uuid,
                        String.format("Test Name %s", uuid),
                        String.format("Test Author %s", uuid)))
                .peek(System.out::println)
                .collect(Collectors.toConcurrentMap(
                        BookDetails::id,
                        bookDetails -> new BookAvailability(bookDetails, ThreadLocalRandom.current().nextInt(25))));
    }
}
