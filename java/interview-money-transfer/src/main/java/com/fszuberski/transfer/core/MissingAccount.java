package com.fszuberski.transfer.core;

public class MissingAccount extends RuntimeException {
    public MissingAccount() {
        super("Account not found");
    }
}
