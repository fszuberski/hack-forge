package com.fszuberski.transfer.core;

import java.util.UUID;

public interface TransferStore {
    AccountBalance account(UUID id);
    void saveBalance(AccountBalance account);
    void saveTransfer(TransferReceipt receipt, String idempotencyKey);
}
