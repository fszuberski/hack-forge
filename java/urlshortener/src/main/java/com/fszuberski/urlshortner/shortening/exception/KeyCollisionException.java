package com.fszuberski.urlshortner.shortening.exception;

public class KeyCollisionException extends RuntimeException {
    public KeyCollisionException(String key) {
        super("Key already exists with different uri: %s".formatted(key));
    }
}
