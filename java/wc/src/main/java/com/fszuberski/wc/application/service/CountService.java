package com.fszuberski.wc.application.service;

import com.fszuberski.wc.application.domain.CountResult;
import com.fszuberski.wc.application.domain.CountType;
import com.fszuberski.wc.application.port.in.CountUseCase;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

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

        return Optional.of(
                StreamSupport.stream(spliterator, false)
                .collect(counting(types)));
    }

    public static class MutableCountResult implements Consumer<String> {
        private final Set<CountType> countTypes;
        private final Map<CountType, Long> resultsPerType;

        public MutableCountResult(Set<CountType> countTypes) {
            this.countTypes = countTypes;
            this.resultsPerType = new HashMap<>();
        }

        public Set<Map.Entry<CountType, Long>> entries() {
            return resultsPerType.entrySet();
        }

        @Override
        public void accept(String line) {
            resultsPerType.put(CountType.LINES, resultsPerType.getOrDefault(CountType.LINES, 0L) + 1);
            // TODO do the actual counts here
        }

        public MutableCountResult combine(MutableCountResult other) {
            other.entries()
                    .forEach(entry ->
                            resultsPerType.put(
                                    entry.getKey(),
                                    entry.getValue() + resultsPerType.getOrDefault(entry.getKey(), 0L)));

            return this;
        }
    }

    public static Collector<String, MutableCountResult, CountResult> counting(Set<CountType> countTypes) {
        return new Collector<>() {
            @Override
            public Supplier<MutableCountResult> supplier() {
                return () -> new MutableCountResult(countTypes);
            }

            @Override
            public BiConsumer<MutableCountResult, String> accumulator() {
                return MutableCountResult::accept;
            }

            @Override
            public BinaryOperator<MutableCountResult> combiner() {
                return MutableCountResult::combine;
            }

            @Override
            public Function<MutableCountResult, CountResult> finisher() {
                return mutableCountResult -> CountResult.of(mutableCountResult.resultsPerType);
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of(Characteristics.UNORDERED);
            }
        };
    }
}
