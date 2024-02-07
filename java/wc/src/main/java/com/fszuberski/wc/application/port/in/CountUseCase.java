package com.fszuberski.wc.application.port.in;

import com.fszuberski.wc.application.domain.CountResult;
import com.fszuberski.wc.application.domain.CountType;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public interface CountUseCase {
    Optional<CountResult> count(Supplier<String> bufferedInputSupplier, Set<CountType> types);
}
