package com.fszuberski.urlshortner.shortening.port.out;


import java.net.URL;

public interface SaveShortenedUrlPort {
    String saveShortenedUrl(URL longUrl, String key);
}
