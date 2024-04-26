package com.fszuberski.urlshortener.common;

import java.net.URI;
import java.util.function.Supplier;

public class Validation {

    public static void validate(boolean value) {
        validate(value, IllegalArgumentException::new);
    }

    public static void validate(boolean value, Supplier<? extends RuntimeException> exceptionSupplier) {
        if (!value) {
            throw exceptionSupplier.get();
        }
    }

    public static boolean isValidUrl(String url) {
        try {
            //noinspection ResultOfMethodCallIgnored
            new URI(url).toURL();
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
