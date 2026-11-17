package com.fszuberski.transfer.core;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalance(UUID id, BigDecimal balance, String currency) {
    public AccountBalance withBalance(BigDecimal updated) {
        return new AccountBalance(id, updated, currency);
    }
}
