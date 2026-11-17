package com.fszuberski.transfer.adapter.http;

import com.fszuberski.transfer.core.TransferCommand;
import com.fszuberski.transfer.core.TransferReceipt;
import com.fszuberski.transfer.core.TransferUseCase;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TransferController {
    private final TransferUseCase transfers;

    public TransferController(TransferUseCase transfers) {
        this.transfers = transfers;
    }

    @PostMapping("/transfers")
    public TransferReceipt transfer(@RequestBody TransferCommand request,
            @RequestHeader(value = "Idempotency-Key", required = false) String key) {
        // Only structural money validation is delivered, not the target business guarantees.
        if (request.sourceAccountId() == null || request.targetAccountId() == null
                || request.amount() == null || request.amount().signum() <= 0
                || request.amount().scale() > 2 || request.currency() == null
                || !Set.of("USD", "EUR").contains(request.currency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transfer");
        }
        // TODO: require/normalize the key, validate account currencies and reject self transfers.
        return transfers.transfer(request, key);
    }
}
