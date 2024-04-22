package com.fszuberski.urlshortner.shortening.port.in;

import com.fszuberski.urlshortner.shortening.domain.ShorteningResult;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public interface ShortenUrlUseCase {
    ShorteningResult shortenUrl(URL longUrl) throws URISyntaxException, MalformedURLException;
}
