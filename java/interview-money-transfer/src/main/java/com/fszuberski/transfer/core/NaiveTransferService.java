package com.fszuberski.transfer.core;

import java.util.UUID;

/** Interview starter only: each persistence call commits independently. Not safe for real money. */
public class NaiveTransferService implements TransferUseCase {
    private final TransferStore store;

    public NaiveTransferService(TransferStore store) {
        this.store = store;
    }

    @Override
    public TransferReceipt transfer(TransferCommand command, String idempotencyKey) {
        // TODO: validate business rules, prevent overdrafts and concurrent lost updates.
        var source = store.account(command.sourceAccountId());
        var debited = source.withBalance(source.balance().subtract(command.amount()));
        store.saveBalance(debited);

        var target = store.account(command.targetAccountId());
        var credited = target.withBalance(target.balance().add(command.amount()));
        store.saveBalance(credited);

        var receipt = new TransferReceipt(UUID.randomUUID(), source.id(), target.id(),
                command.amount(), command.currency(), debited.balance(), credited.balance());
        // TODO: one atomic operation, two balanced ledger entries, and race-safe idempotency.
        store.saveTransfer(receipt, idempotencyKey);
        return receipt;
    }
}
