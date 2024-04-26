package com.fszuberski.urlshortener.core.shortening.port.in;

import com.fszuberski.urlshortener.core.shortening.domain.ShorteningResult;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public interface ShortenUrlUseCase {
    ShorteningResult shortenUrl(URL longUrl) throws URISyntaxException, MalformedURLException;
}
