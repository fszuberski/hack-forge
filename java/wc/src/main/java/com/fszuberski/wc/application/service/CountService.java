package com.fszuberski.wc.application.service;

import com.fszuberski.wc.application.domain.CountResult;
import com.fszuberski.wc.application.domain.CountType;
import com.fszuberski.wc.application.port.in.CountUseCase;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Set;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

public class CountService implements CountUseCase {

    public Optional<CountResult> count(byte[] bytes, Charset charset, Set<CountType> types) {

        // temporary
        return Optional.of(
                CountResult.of(
                        types.stream()
                                .collect(toMap(identity(), type -> 0L))
                )
        );

    }
}
