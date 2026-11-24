package com.fszuberski.ticket.office;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TicketService {

    private final AtomicInteger numberOfAvailableTickets;
    private final ConcurrentHashMap<UUID, OperationResult> transactionHistory;

    public TicketService(int initialNumberOfTickets) {
        if (initialNumberOfTickets <= 0) {
            throw new IllegalArgumentException("number of tickets must be non-negative");
        }

        this.numberOfAvailableTickets = new AtomicInteger(initialNumberOfTickets);
        this.transactionHistory = new ConcurrentHashMap<>();
    }

    public OperationResult buy(UUID uuid) {
        return buy(uuid, 1);
    }

    public OperationResult buy(UUID uuid, int numberOfTickets) {
        if (numberOfTickets <= 0) {
            throw new IllegalArgumentException("number of tickets must be positiveq");
        }

        var transaction = transactionHistory.computeIfAbsent(uuid, _ -> {
            var initialValue = numberOfAvailableTickets.getAndUpdate(
                    current -> current >= numberOfTickets ? current - numberOfTickets : current);
            var purchased = initialValue >= numberOfTickets;

            return new OperationResult(
                    purchased ? Result.SUCCESS : Result.FAILURE,
                    numberOfTickets,
                    numberOfAvailableTickets.get());
        });

        if (numberOfTickets != transaction.numberOfRequestedTickets()) {
            return new OperationResult(Result.CONFLICT, numberOfTickets, numberOfAvailableTickets.get());
        }

        return transaction;
    }

    public record OperationResult(Result result, int numberOfRequestedTickets, int numberOfRemainingTickets) {

    }

    public enum Result {
        SUCCESS,
        FAILURE,
        CONFLICT
    }

}
