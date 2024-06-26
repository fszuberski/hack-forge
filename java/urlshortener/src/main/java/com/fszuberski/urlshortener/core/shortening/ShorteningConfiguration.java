package com.fszuberski.urlshortener.core.shortening;

import java.util.Objects;

import static com.fszuberski.urlshortener.common.Utils.lastCharacter;
import static com.fszuberski.urlshortener.common.Validation.isValidUrl;
import static com.fszuberski.urlshortener.common.Validation.validate;

public record ShorteningConfiguration(String baseUrl, int keyLength, int maxAttemptsOnCollisions) {

    public ShorteningConfiguration {
        validate(isValidUrl(baseUrl) && Objects.equals(lastCharacter(baseUrl), '/'));
        validate(keyLength >= 8);
        validate(maxAttemptsOnCollisions >= 16);
    }
}
