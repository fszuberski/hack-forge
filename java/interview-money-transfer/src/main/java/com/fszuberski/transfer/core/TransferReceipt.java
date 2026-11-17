package com.fszuberski.transfer.core;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferReceipt(UUID transactionId, UUID sourceAccountId, UUID targetAccountId,
                              BigDecimal amount, String currency,
                              BigDecimal sourceBalance, BigDecimal targetBalance) {}
