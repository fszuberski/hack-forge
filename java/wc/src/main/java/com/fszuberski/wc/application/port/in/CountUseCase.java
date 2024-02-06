package com.fszuberski.wc.application.port.in;

import com.fszuberski.wc.application.domain.CountResult;
import com.fszuberski.wc.application.domain.CountType;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Set;

public interface CountUseCase {
    Optional<CountResult> count(byte[] bytes, Charset charset, Set<CountType> types);
}
