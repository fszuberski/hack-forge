package com.fszuberski.urlshortner.common;

import java.net.URI;
import java.util.function.Supplier;

public class Validation {

    public static void validate(boolean value) {
        validate(value, IllegalArgumentException::new);
    }

    public static void validate(boolean value, Supplier<RuntimeException> exceptionSupplier) {
        if (!value) {
            throw exceptionSupplier.get();
        }
    }

    public static boolean isValidUri(String uri) {
        try {
            new URI(uri);
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
