package com.fszuberski.transfer.core;

public interface TransferUseCase {
    TransferReceipt transfer(TransferCommand command, String idempotencyKey);
}
