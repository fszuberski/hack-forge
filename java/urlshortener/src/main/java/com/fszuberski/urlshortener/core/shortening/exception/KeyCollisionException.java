package com.fszuberski.urlshortener.core.shortening.exception;

public class KeyCollisionException extends RuntimeException {
    public KeyCollisionException(String key) {
        super("Key already exists with different url: %s".formatted(key));
    }
}
