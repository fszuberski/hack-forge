package com.fszuberski.wc.adapter.cli;

public class InvalidFlagException extends RuntimeException {

    public InvalidFlagException(String message) {
        super("Invalid command line flag provided: %s".formatted(message));
    }
}
