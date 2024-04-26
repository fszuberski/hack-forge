package com.fszuberski.urlshortener.core.shortening.port.out;

public interface SaveShortenedUrlPort {
    String saveShortenedUrl(String url, String key);
}
