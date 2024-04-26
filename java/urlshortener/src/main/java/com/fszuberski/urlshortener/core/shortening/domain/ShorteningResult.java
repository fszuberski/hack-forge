package com.fszuberski.urlshortener.core.shortening.domain;

import java.net.URL;

public record ShorteningResult(URL longUrl, URL shortUrl) {
}
