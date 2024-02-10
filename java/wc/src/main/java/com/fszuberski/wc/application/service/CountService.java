package com.fszuberski.wc.application.service;

import com.fszuberski.wc.application.domain.CountResult;
import com.fszuberski.wc.application.domain.CountType;
import com.fszuberski.wc.application.port.in.CountUseCase;

import java.util.*;

import static com.fszuberski.wc.application.service.CountCollector.counting;

public class CountService implements CountUseCase {

    public CountResult count(BufferedInputSupplier bufferedInputSupplier, Set<CountType> types) {
        return bufferedInputSupplier.stream()
                .collect(counting(types));
    }
}
