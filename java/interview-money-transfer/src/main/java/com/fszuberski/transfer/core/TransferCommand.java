package com.fszuberski.transfer.core;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferCommand(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, String currency) {}
