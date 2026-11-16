package com.fszuberski.multithreading.collections;

import java.util.Map;

/** Exercise 06B. A check-then-decrement must be one atomic per-key operation. */
public final class AtomicInventory {
    public AtomicInventory(Map<String, Integer> initialStock) {
        // TODO 06B: validate and defensively copy into concurrent storage.
    }

    public boolean reserve(String sku, int quantity) {
        throw new UnsupportedOperationException("TODO 06B: never oversell");
    }

    public int available(String sku) {
        throw new UnsupportedOperationException("TODO 06B: unknown SKU has zero stock");
    }
}
