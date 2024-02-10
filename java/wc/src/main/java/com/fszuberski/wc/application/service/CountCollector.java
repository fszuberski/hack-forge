package com.fszuberski.wc.application.service;

import com.fszuberski.wc.application.domain.CountResult;
import com.fszuberski.wc.application.domain.CountType;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

public class CountCollector {

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

    public static class MutableCountResult implements Consumer<String> {
        private final Set<CountType> countTypes;
        private final Map<CountType, Long> resultsPerType;

        private static final Map<CountType, BiConsumer<String, Map<CountType, Long>>> ops = Map.of(

                CountType.BYTES, (line, results) ->
                        results.put(CountType.BYTES, results.getOrDefault(CountType.BYTES, 0L) + line.getBytes(StandardCharsets.UTF_8).length),

                CountType.CHARACTERS, (line, results) ->
                        results.put(CountType.CHARACTERS, results.getOrDefault(CountType.CHARACTERS, 0L) + line.length()),

                CountType.WORDS, (line, results) ->
                        results.put(CountType.WORDS, results.getOrDefault(CountType.WORDS, 0L) + line.trim().split("\\s+").length),

                CountType.LINES, (line, results) ->
                        results.put(CountType.LINES, results.getOrDefault(CountType.LINES, 0L) + 1)

        );

        public MutableCountResult(Set<CountType> countTypes) {
            this.countTypes = countTypes;
            this.resultsPerType = new HashMap<>();
        }

        public Set<Map.Entry<CountType, Long>> entries() {
            return resultsPerType.entrySet();
        }

        @Override
        public void accept(String line) {
            countTypes.stream()
                    .map(ops::get)
                    .filter(Objects::nonNull)
                    .forEach(op -> op.accept(line, resultsPerType));
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
}
