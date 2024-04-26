package com.fszuberski.urlshortener.common;

import java.util.Collection;
import java.util.function.Function;

public class Utils {

    public static <T> T retryOnException(Function<Integer, T> retryableFn, int maxRetries, Collection<Class<? extends Exception>> retryableExceptions) {
        var retry = false;
        var round = 1;
        T result = null;
        do {
            retry = false;
            try {
                result = retryableFn.apply(round);
            } catch (Exception e) {
                if (round >= maxRetries) {
                    throw e;
                }

                if (instanceOfAny(e, retryableExceptions)) {
                    retry = true;
                    round++;
                } else {
                    throw e;
                }
            }
        } while (retry && round <= maxRetries);

        return result;
    }

    public static <T> boolean instanceOfAny(T item, Collection<Class<? extends T>> classes) {
        return classes
                .stream()
                .anyMatch(cls -> cls.isInstance(item));
    }

    public static Character lastCharacter(String str) {
        return str != null
                ? str.charAt(str.length() - 1)
                : null;
    }
}
