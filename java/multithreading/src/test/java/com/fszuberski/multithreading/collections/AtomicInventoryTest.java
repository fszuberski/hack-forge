package com.fszuberski.multithreading.collections;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Nested;

import java.util.HashMap;
import java.util.Map;

import static com.fszuberski.multithreading.support.TestThreads.race;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class AtomicInventoryTest {
    @Test
    void shouldPreventOversellingGivenConcurrentReservations() throws Exception {
        var inventory = new AtomicInventory(Map.of("book", 5));
        var results = race(16, () -> inventory.reserve("book", 1));
        assertEquals(5, results.stream().filter(Boolean::booleanValue).count());
        assertEquals(0, inventory.available("book"));
        assertFalse(inventory.reserve("unknown", 1));
    }

    @Test
    void shouldPreserveOwnedStockGivenInputMutationAndRejectedReservation() {
        var input = new HashMap<>(Map.of("book", 2));
        var inventory = new AtomicInventory(input);
        input.put("book", 100);
        assertFalse(inventory.reserve("book", 3));
        assertEquals(2, inventory.available("book"));
    }

    @Nested
    class InvalidStock {
        @Test
        void shouldRejectReservationGivenZeroQuantity() {
            var inventory = new AtomicInventory(Map.of("book", 0));
            assertThrows(IllegalArgumentException.class, () -> inventory.reserve("book", 0));
        }

        @Test
        void shouldRejectInitialStockGivenNegativeQuantity() {
            assertThrows(IllegalArgumentException.class, () -> new AtomicInventory(Map.of("book", -1)));
        }
    }
}
