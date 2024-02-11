package com.fszuberski.wc.application.service;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BufferedInputSupplier extends Spliterators.AbstractSpliterator<String> {

    private final Supplier<String> inputSupplier;

    private BufferedInputSupplier(Supplier<String> inputSupplier) {
        super(Long.MAX_VALUE, Spliterator.ORDERED);
        this.inputSupplier = inputSupplier;
    }

    public Stream<String> stream() {
        return stream(true);
    }

    public Stream<String> stream(boolean parallel) {
        return StreamSupport.stream(this, parallel);
    }

    @Override
    public boolean tryAdvance(Consumer<? super String> action) {
        final String line;
        if ((line = inputSupplier.get()) != null) {
            action.accept(line);
            return true;
        }

        return false;
    }

    public static BufferedInputSupplier from(Supplier<String> inputSupplier) {
        return new BufferedInputSupplier(inputSupplier);
    }
}
