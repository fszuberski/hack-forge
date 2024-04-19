package com.fszuberski.urlshortner.shortening.service;

import com.fszuberski.urlshortner.shortening.ShorteningConfiguration;
import com.fszuberski.urlshortner.shortening.domain.ShorteningResult;
import com.fszuberski.urlshortner.shortening.exception.KeyCollisionException;
import com.fszuberski.urlshortner.shortening.port.in.ShortenUriUseCase;
import com.fszuberski.urlshortner.shortening.port.out.SaveShortenedUriPort;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.fszuberski.urlshortner.common.Hashing.generateShortHash;
import static com.fszuberski.urlshortner.common.Utils.retryOnException;
import static com.fszuberski.urlshortner.common.Validation.validate;

@Slf4j
public class ShorteningService implements ShortenUriUseCase {

    private final SaveShortenedUriPort saveShortenedUriPort;
    private final ShorteningConfiguration configuration;

    public ShorteningService(SaveShortenedUriPort saveShortenedUriPort, ShorteningConfiguration configuration) {
        validate(saveShortenedUriPort != null);
        validate(configuration != null);

        this.saveShortenedUriPort = saveShortenedUriPort;
        this.configuration = configuration;
    }

    @Override
    public ShorteningResult shortenUri(URI longUri) throws URISyntaxException {
        validate(longUri != null);

        var key = retryOnException(
                attempt -> saveShortenedUriPort.saveShortenedUri(
                        longUri,
                        generateShortHash(longUri.toString(), attempt, configuration.keyLength())),
                configuration.maxAttemptsOnCollisions(),
                List.of(KeyCollisionException.class));

        return new ShorteningResult(longUri, new URI("%s%s".formatted(configuration.baseUrl(), key)));
    }
}
