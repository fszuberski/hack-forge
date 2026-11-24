package com.fszuberski.ticket.office;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceTest {

    @Nested
    class Constructor {

        @ParameterizedTest
        @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
        public void shouldThrowExceptionGivenInitialStockIsNotPositive(int initialStock) {
            assertThrows(IllegalArgumentException.class, () -> new TicketService(initialStock));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 100, Integer.MAX_VALUE})
        public void shouldInitializeObjectGivenInitialStockIsPositive(int initialStock) {
            var result = new TicketService(initialStock);
            assertNotNull(result);
        }
    }

    @Nested
    class Buy {

        @ParameterizedTest
        @ValueSource(ints = {1, 100, Integer.MAX_VALUE})
        void shouldReturnPositiveResultGivenStockIsNonNegative(int initialNumberOfTickets) {
            // given: stock is non-negative (tickets are available)
            var ticketService = new TicketService(initialNumberOfTickets);

            // when: a customer requests to buy a ticket
            var result = ticketService.buy(UUID.randomUUID());

            // then: a successful result is returned
            assertNotNull(result);
            assertEquals(TicketService.Result.SUCCESS, result.result());

            // and: initialNumberOfTickets - 1 are remaining
            assertEquals(initialNumberOfTickets - 1, result.numberOfRemainingTickets());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
        void shouldThrowExceptionGivenNumberOfTicketsToBuyIsNonPositive(int ticketsToBuy) {
            // given: stock is 1
            var ticketService = new TicketService(1);

            // when: a customer requests to buy a non-positive number of tickets
            assertThrows(IllegalArgumentException.class, () -> ticketService.buy(UUID.randomUUID(), ticketsToBuy));
        }

        @Test
        void shouldReturnNegativeResultGivenStockIsZero() {
            // given: stock is 0
            var ticketService = new TicketService(1);
            ticketService.buy(UUID.randomUUID()); // depletes available tickets

            // when: a customer requests to buy a ticket
            var result = ticketService.buy(UUID.randomUUID());

            // then: a negative result is returned
            assertNotNull(result);
            assertEquals(TicketService.Result.FAILURE, result.result());

            // and: 0 tickets are remaining
            assertEquals(0, Math.max(result.numberOfRemainingTickets(), 0));
        }

        @Test
        void shouldReturnOriginalResultGivenUuidWasAlreadyProcessed() {
            // given: stock is 1
            var uuid = UUID.randomUUID();
            var ticketService = new TicketService(1);
            ticketService.buy(uuid, 1); // uses the uuid and also depletes the available tickets

            // when: a customer requests to buy a ticket with a used uuid, but with the same quantity
            var result = ticketService.buy(uuid, 1);

            // then: a successful result is returned
            assertNotNull(result);
            assertEquals(TicketService.Result.SUCCESS, result.result());
            assertEquals(0, result.numberOfRemainingTickets());
        }

        @Test
        void shouldReturnConflictResultGivenUuidWasAlreadyProcessed() {
            // given: stock is 3
            var uuid = UUID.randomUUID();
            var ticketService = new TicketService(3);
            ticketService.buy(uuid, 2); // uses the uuid, doesn't deplete available tickets

            // when: a customer requests to buy a ticket with a used uuid, but with a different quantity
            var result = ticketService.buy(uuid, 1);

            // then: a conflict result is returned, even if tickets are available
            assertNotNull(result);
            assertEquals(TicketService.Result.CONFLICT, result.result());
            assertEquals(1, result.numberOfRemainingTickets());
        }

        @RepeatedTest(100)
        void shouldReturnOnePositiveResultGivenMultipleConcurrentRequestsWithTheSameUuid() {
            // given: available stock is 10
            var ticketService = new TicketService(10);

            // when: buy is invoked concurrently 10 times with the same uuid
            var uuid = UUID.randomUUID();

            var originalResult = ticketService.buy(uuid, 2);
            Callable<TicketService.OperationResult> subsequentBuyInvocation = () -> ticketService.buy(uuid);
            var buyInvocations = IntStream.rangeClosed(1, 9).mapToObj(_ -> subsequentBuyInvocation).toList();
            withTestExecutorService(executor -> {
                var operationResultStream = executor.invokeAll(buyInvocations, 1, TimeUnit.SECONDS)
                        .stream()
                        .map(TicketServiceTest.this::getFutureResult);

                var grouped = operationResultStream
                        .collect(Collectors.groupingBy(TicketService.OperationResult::result));

                // then: the original buy invocation succeeded
                assertEquals(TicketService.Result.SUCCESS, originalResult.result());

                // and: 0 buy succeeded, 0 buys failed, 9 buys conflicted
                var successful = grouped.getOrDefault(TicketService.Result.SUCCESS, List.of());
                var failed = grouped.getOrDefault(TicketService.Result.FAILURE, List.of());
                var conflicted = grouped.getOrDefault(TicketService.Result.CONFLICT, List.of());

                assertEquals(0, successful.size());
                assertEquals(0, failed.size());
                assertEquals(9, conflicted.size());
            });
        }

        @RepeatedTest(100)
        void shouldReturnNegativeResultForACustomerGivenNotEnoughTickets() {
            // given: available stock is 10
            var ticketService = new TicketService(10);

            // when: 4 customers try buying more 12 tickets in total
            Callable<TicketService.OperationResult> buyInvocation = () -> ticketService.buy(UUID.randomUUID(), 3);
            var buyInvocations = IntStream.rangeClosed(1, 4).mapToObj(_ -> buyInvocation).toList();

            withTestExecutorService(executor -> {
                var operationResultStream = executor.invokeAll(buyInvocations, 1, TimeUnit.SECONDS)
                        .stream()
                        .map(TicketServiceTest.this::getFutureResult);

                var partitioned = operationResultStream
                        .collect(Collectors.partitioningBy(operationResult ->
                                TicketService.Result.SUCCESS.equals(operationResult.result())));

                // then: 3 buys succeeded, 1 buy failed
                var successful = partitioned.get(true);
                var failed = partitioned.get(false);

                assertEquals(3, successful.size());
                assertEquals(1, failed.size());

                // and: there is only 1 ticker remaining for the failed buy
                assertEquals(1, failed.size());
            });
        }

        @RepeatedTest(100)
        void shouldAllowOnlyOneBuyerToBuyTicketGivenOneTicketRemaining() {
            // given: available stock is 1
            var ticketService = new TicketService(1);

            // when: 10 customers request to buy a ticket at the same time
            var buyInvocations = getBuyInvocations(ticketService, 10);

            withTestExecutorService(executor -> {
                var operationResultStream = executor.invokeAll(buyInvocations, 1, TimeUnit.SECONDS)
                        .stream()
                        .map(TicketServiceTest.this::getFutureResult);

                var partitioned = operationResultStream
                        .collect(Collectors.partitioningBy(operationResult -> TicketService.Result.SUCCESS.equals(operationResult.result())));

                // then: only one customer successful in buying a ticket
                var successful = partitioned.get(true);
                assertEquals(1, successful.size());

                // and: 9 customers didn't succeed in buying tickets
                var failed = partitioned.get(false);
                assertEquals(9, failed.size());

                // and: the number of remaining tickets is 0 for all results
                var allMatch = Stream.concat(successful.stream(), failed.stream())
                        .allMatch(operationResult -> operationResult.numberOfRemainingTickets() == 0);
                assertTrue(allMatch);
            });
        }

        @RepeatedTest(100)
        void shouldReturnCorrectNumberOfAvailableTicketsGivenMultipleInvocations() {
            // given: available stock is 100
            var ticketService = new TicketService(100);

            // when: multiple customers request to buy 50 tickets
            var buyInvocations = getBuyInvocations(ticketService, 49);
            withTestExecutorService(executor -> {
                //noinspection ResultOfMethodCallIgnored
                executor.invokeAll(buyInvocations, 1, TimeUnit.SECONDS)
                        .stream()
                        .map(TicketServiceTest.this::getFutureResult);

                var finalResult = ticketService.buy(UUID.randomUUID());

                // then: the remaining tickets returned by the final invocation is 50
                assertEquals(50, finalResult.numberOfRemainingTickets());
            });
        }
    }

    @FunctionalInterface
    public interface ExecutionBlock<T> {
        void accept(T t) throws Exception;
    }

    private void withTestExecutorService(ExecutionBlock<ExecutorService> block) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            block.accept(executor);
        } catch (Exception e) {
            fail("test setup failed");
        }
    }

    private List<Callable<TicketService.OperationResult>> getBuyInvocations(TicketService ticketService, int count) {
        Callable<TicketService.OperationResult> buyInvocation = () -> ticketService.buy(UUID.randomUUID());
        return IntStream.rangeClosed(1, count).mapToObj(_ -> buyInvocation).toList();
    }

    private <T> T getFutureResult(Future<T> future) {
        try {
            return future.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}