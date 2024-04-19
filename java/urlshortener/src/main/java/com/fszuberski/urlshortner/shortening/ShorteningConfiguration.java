package com.fszuberski.urlshortner.shortening;

import java.util.Objects;

import static com.fszuberski.urlshortner.common.Utils.lastCharacter;
import static com.fszuberski.urlshortner.common.Validation.isValidUri;
import static com.fszuberski.urlshortner.common.Validation.validate;

public record ShorteningConfiguration(String baseUrl, int keyLength, int maxAttemptsOnCollisions) {

    public ShorteningConfiguration {
        validate(isValidUri(baseUrl) && Objects.equals(lastCharacter(baseUrl), '/'));
        validate(keyLength >= 8);
        validate(maxAttemptsOnCollisions >= 16);
    }
}
