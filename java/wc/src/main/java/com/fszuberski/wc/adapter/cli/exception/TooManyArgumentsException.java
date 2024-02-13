package com.fszuberski.wc.adapter.cli.exception;

public class TooManyArgumentsException extends RuntimeException {

    public TooManyArgumentsException() {
        super("Too many arguments provided.");
    }
}
