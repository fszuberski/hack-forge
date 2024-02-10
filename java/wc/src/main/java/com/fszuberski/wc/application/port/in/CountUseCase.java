package com.fszuberski.wc.application.port.in;

import com.fszuberski.wc.application.service.BufferedInputSupplier;
import com.fszuberski.wc.application.domain.CountResult;
import com.fszuberski.wc.application.domain.CountType;

import java.util.Set;

public interface CountUseCase {
    CountResult count(BufferedInputSupplier bufferedInputSupplier, Set<CountType> types);
}
