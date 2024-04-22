package com.fszuberski.urlshortner.shortening.service;

import com.fszuberski.urlshortner.shortening.ShorteningConfiguration;
import com.fszuberski.urlshortner.shortening.domain.ShorteningResult;
import com.fszuberski.urlshortner.shortening.exception.KeyCollisionException;
import com.fszuberski.urlshortner.shortening.port.in.ShortenUrlUseCase;
import com.fszuberski.urlshortner.shortening.port.out.SaveShortenedUrlPort;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static com.fszuberski.urlshortner.common.Hashing.generateShortHash;
import static com.fszuberski.urlshortner.common.Utils.retryOnException;
import static com.fszuberski.urlshortner.common.Validation.validate;

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
                        longUrl,
                        generateShortHash(longUrl.toString(), attempt, configuration.keyLength())),
                configuration.maxAttemptsOnCollisions(),
                List.of(KeyCollisionException.class));

        return new ShorteningResult(longUrl, new URI("%s%s".formatted(configuration.baseUrl(), key)).toURL());
    }
}
