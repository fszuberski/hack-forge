package com.fszuberski.wc.application.service;

import com.fszuberski.wc.application.domain.CountResult;
import com.fszuberski.wc.application.domain.CountType;
import com.fszuberski.wc.application.port.in.CountUseCase;

import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

public class CountService implements CountUseCase {

    public Optional<CountResult> count(Supplier<String> bufferedInputSupplier, Set<CountType> types) {

        var spliterator = new Spliterators.AbstractSpliterator<String>(Long.MAX_VALUE, Spliterator.ORDERED) {
            @Override
            public boolean tryAdvance(Consumer<? super String> action) {
                final String line;
                if ((line = bufferedInputSupplier.get()) != null) {
                    action.accept(line);
                    return true;
                }

                return false;
            }
        };

        StreamSupport.stream(spliterator, false)
                .forEach(System.out::println);

        // temporary
        return Optional.of(
                CountResult.of(
                        types.stream()
                                .collect(toMap(identity(), type -> 0L))
                )
        );

    }
}
