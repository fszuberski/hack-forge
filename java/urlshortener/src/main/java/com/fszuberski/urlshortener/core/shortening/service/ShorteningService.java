package com.fszuberski.urlshortener.core.shortening.service;

import com.fszuberski.urlshortener.core.shortening.ShorteningConfiguration;
import com.fszuberski.urlshortener.core.shortening.domain.ShorteningResult;
import com.fszuberski.urlshortener.core.shortening.exception.KeyCollisionException;
import com.fszuberski.urlshortener.core.shortening.port.in.ShortenUrlUseCase;
import com.fszuberski.urlshortener.core.shortening.port.out.SaveShortenedUrlPort;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static com.fszuberski.urlshortener.common.Hashing.generateShortHash;
import static com.fszuberski.urlshortener.common.Utils.retryOnException;
import static com.fszuberski.urlshortener.common.Validation.validate;

@Slf4j
public class ShorteningService implements ShortenUrlUseCase {

    private final SaveShortenedUrlPort saveShortenedUrlPort;
    private final ShorteningConfiguration configuration;

    public ShorteningService(SaveShortenedUrlPort saveShortenedUrlPort, ShorteningConfiguration configuration) {
        validate(saveShortenedUrlPort != null);
        validate(configuration != null);

        this.saveShortenedUrlPort = saveShortenedUrlPort;
        this.configuration = configuration;
    }

    @Override
    public ShorteningResult shortenUrl(URL longUrl) throws URISyntaxException, MalformedURLException {
        validate(longUrl != null);

        var key = retryOnException(
                attempt -> saveShortenedUrlPort.saveShortenedUrl(
                        longUrl.toString(),
                        generateShortHash(longUrl.toString(), attempt, configuration.keyLength())),
                configuration.maxAttemptsOnCollisions(),
                List.of(KeyCollisionException.class));

        return new ShorteningResult(longUrl, new URI("%s%s".formatted(configuration.baseUrl(), key)).toURL());
    }
}
