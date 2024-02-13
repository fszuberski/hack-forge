package com.fszuberski.wc.adapter.cli.exception;

public class FilePathNotProvidedException extends RuntimeException {

    public FilePathNotProvidedException() {
        super("File path must be provided.");
    }
}
