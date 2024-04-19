package com.fszuberski.urlshortner.shortening.port.in;

import com.fszuberski.urlshortner.shortening.domain.ShorteningResult;

import java.net.URI;
import java.net.URISyntaxException;

public interface ShortenUriUseCase {
    ShorteningResult shortenUri(URI longUri) throws URISyntaxException;
}
