package com.fszuberski.urlshortener.adapter.datasource;

import com.fszuberski.urlshortener.core.shortening.exception.KeyCollisionException;
import com.fszuberski.urlshortener.core.shortening.port.out.SaveShortenedUrlPort;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class InMemoryDatasourceAdapter implements SaveShortenedUrlPort {

    private final ConcurrentHashMap<String, String> keyToUrlMap;

    public InMemoryDatasourceAdapter() {
        this.keyToUrlMap = new ConcurrentHashMap<>();
    }

    @Override
    public String saveShortenedUrl(String url, String generatedKey) {
        // The compute(...) operation is atomic for ConcurrentHashMap
        keyToUrlMap.compute(generatedKey, (key, value) -> {
            if (value != null && !value.equalsIgnoreCase(url)) {
                throw new KeyCollisionException(value);
            }

            return url;
        });

        log.info("Map post-insert: {}", keyToUrlMap);

        return generatedKey;
    }
}
