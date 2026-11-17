package com.fszuberski.transfer.adapter.persistence;

import com.fszuberski.transfer.core.TransferReceipt;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class Transaction {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID sourceAccountId;
    @Column(nullable = false)
    private UUID targetAccountId;
    @Column(nullable = false, columnDefinition = "numeric")
    private BigDecimal amount;
    @Column(nullable = false)
    private String currency;
    private String idempotencyKey;
    @Column(nullable = false, columnDefinition = "numeric")
    private BigDecimal sourceBalance;
    @Column(nullable = false, columnDefinition = "numeric")
    private BigDecimal targetBalance;

    protected Transaction() {}

    public Transaction(TransferReceipt receipt, String idempotencyKey) {
        id = receipt.transactionId();
        sourceAccountId = receipt.sourceAccountId();
        targetAccountId = receipt.targetAccountId();
        amount = receipt.amount();
        currency = receipt.currency();
        sourceBalance = receipt.sourceBalance();
        targetBalance = receipt.targetBalance();
        this.idempotencyKey = idempotencyKey;
    }
}
