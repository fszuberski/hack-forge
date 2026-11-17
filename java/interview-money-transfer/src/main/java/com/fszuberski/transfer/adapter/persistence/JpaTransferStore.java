package com.fszuberski.transfer.adapter.persistence;

import com.fszuberski.transfer.core.AccountBalance;
import com.fszuberski.transfer.core.MissingAccount;
import com.fszuberski.transfer.core.TransferReceipt;
import com.fszuberski.transfer.core.TransferStore;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaTransferStore implements TransferStore {
    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public JpaTransferStore(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Override
    public AccountBalance account(UUID id) {
        var account = accounts.findById(id).orElseThrow(MissingAccount::new);
        return new AccountBalance(account.getId(), account.getBalance(), account.getCurrency());
    }

    @Override
    public void saveBalance(AccountBalance account) {
        accounts.save(new Account(account.id(), account.balance(), account.currency()));
    }

    @Override
    public void saveTransfer(TransferReceipt receipt, String idempotencyKey) {
        transactions.save(new Transaction(receipt, idempotencyKey));
    }
}
