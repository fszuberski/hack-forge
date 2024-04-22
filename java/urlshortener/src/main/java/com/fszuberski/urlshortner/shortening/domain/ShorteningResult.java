package com.fszuberski.urlshortner.shortening.domain;

import java.net.URL;

public record ShorteningResult(URL longUrl, URL shortUrl) {
}
