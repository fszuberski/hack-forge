package com.fszuberski.wc.adapter.cli;

import com.fszuberski.wc.application.domain.CountType;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;

public class CommandLineArguments {
    private final Set<CountType> countTypes;
    private final String filePath;

    private static final Map<String, CountType> flagToCountType = Map.of(
            "c", CountType.BYTES,
            "l", CountType.LINES,
            "w", CountType.WORDS,
            "m", CountType.CHARACTERS
    );

    private static final Set<CountType> defaultCountTypes = Set.of(
            CountType.BYTES, CountType.LINES, CountType.CHARACTERS, CountType.WORDS
    );

    private CommandLineArguments(String[] arguments) {
        var partitioned = Arrays.stream(arguments)
                .collect(partitioningBy(arg -> arg.startsWith("-")));

        if (partitioned.get(Boolean.FALSE).isEmpty()) {
            throw new RuntimeException("File path must be provided.");
        }

        if (partitioned.get(Boolean.FALSE).size() > 1) {
            throw new RuntimeException("Too many arguments provided.");
        }

        this.filePath = partitioned.get(Boolean.FALSE)
                .stream()
                .findFirst()
                .orElseThrow();

        this.countTypes = partitioned.get(Boolean.TRUE)
                .stream()
                .map(group -> group.substring(1))
                .flatMap(group -> Arrays.stream(group.split("")))
                .map(flag -> {
                    if (!flagToCountType.containsKey(flag)) {
                        throw new InvalidFlagException(flag);
                    }
                    return flagToCountType.get(flag);
                })
                .collect(collectingAndThen(toSet(), set -> set.isEmpty() ? defaultCountTypes : set));
    }

    public Set<CountType> countTypes() {
        return countTypes;
    }

    public String filePath() {
        return filePath;
    }

    public static CommandLineArguments from(String[] arguments) {
        return new CommandLineArguments(arguments);
    }
}
