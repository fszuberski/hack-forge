package com.fszuberski.urlshortner.shortening.port.out;


import java.net.URI;

public interface SaveShortenedUriPort {
    String saveShortenedUri(URI longUri, String key);
}
