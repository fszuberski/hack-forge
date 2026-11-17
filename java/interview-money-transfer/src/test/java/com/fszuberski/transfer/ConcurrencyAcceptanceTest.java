package com.fszuberski.transfer;

import com.fszuberski.transfer.testing.data.PostgresApplicationData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Tag("interview")
class ConcurrencyAcceptanceTest extends PostgresApplicationData {
    @Nested
    class Contention {
        @Test
        void shouldCommitOnceGivenConcurrentSameKey() throws Exception {
            // given: one identity and sufficient funds for two requests, so overdraft cannot mask duplication
            var source = account("100.00", "USD");
            var target = account("25.00", "USD");
            var key = key();

            // when: independent request threads start together, each using real database transactions
            var results = together(
                    () -> transfer(source, target, "10.00", "USD", key),
                    () -> transfer(source, target, "10.00", "USD", key));

            // then: both callers observe the original receipt and exactly one committed operation
            assertAll(
                    () -> assertThat(results).allSatisfy(r -> assertThat(r.getResponse().getStatus()).isEqualTo(200)),
                    () -> assertSameReceipt(results.getFirst(), results.getLast()),
                    () -> assertThat(transfers(source)).isEqualTo(1),
                    () -> assertThat(balance(source)).isEqualByComparingTo("90"),
                    () -> assertThat(balance(target)).isEqualByComparingTo("35"));
        }

        @Test
        void shouldRejectOneDebitGivenCompetingRequestsExceedFunds() throws Exception {
            // given: a source that can fund either debit but not both, and independent targets/keys
            var source = account("100.00", "USD");
            var firstTarget = account("0.00", "USD");
            var secondTarget = account("0.00", "USD");
            var firstKey = key();
            var secondKey = key();

            // when: two independent callers compete for the same funds
            var results = together(
                    () -> transfer(source, firstTarget, "80.00", "USD", firstKey),
                    () -> transfer(source, secondTarget, "80.00", "USD", secondKey));

            // then: exactly one transfer succeeds and total credited money is backed by one debit
            assertAll(
                    () -> assertThat(results.stream().map(r -> r.getResponse().getStatus()).toList())
                            .containsExactlyInAnyOrder(200, 409),
                    () -> assertThat(transfers(source)).isEqualTo(1),
                    () -> assertThat(balance(source)).isEqualByComparingTo("20"),
                    () -> assertThat(balance(firstTarget).add(balance(secondTarget))).isEqualByComparingTo("80"));
        }

        @Test
        void shouldCompleteWithConsistentBalancesGivenOpposingTransfers() throws Exception {
            // given: both accounts can fund their transfer regardless of which operation runs first
            var first = account("100.00", "USD");
            var second = account("100.00", "USD");
            var forwardKey = key();
            var reverseKey = key();

            // when: A->B and B->A start concurrently under bounded database and harness timeouts
            var results = together(
                    () -> transfer(first, second, "10.00", "USD", forwardKey),
                    () -> transfer(second, first, "10.00", "USD", reverseKey));

            // then: both complete without deadlock or lost money; a passing schedule is not proof
            assertAll(
                    () -> assertThat(results).allSatisfy(r -> assertThat(r.getResponse().getStatus()).isEqualTo(200)),
                    () -> assertThat(balance(first)).isEqualByComparingTo("100"),
                    () -> assertThat(balance(second)).isEqualByComparingTo("100"),
                    () -> assertThat(transfers(first)).isEqualTo(1),
                    () -> assertThat(transfers(second)).isEqualTo(1));
        }
    }

    private List<MvcResult> together(Callable<MvcResult> first, Callable<MvcResult> second) throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        var futures = new ArrayList<Future<MvcResult>>();
        try {
            for (var action : List.of(first, second)) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new AssertionError("Request start gate timed out");
                    }
                    return action.call();
                }));
            }
            assertThat(ready.await(5, TimeUnit.SECONDS)).as("both request actors ready").isTrue();
            start.countDown();
            var results = new ArrayList<MvcResult>();
            for (var future : futures) {
                results.add(future.get(15, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            start.countDown();
            futures.forEach(future -> future.cancel(true));
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).as("request actors terminated").isTrue();
        }
    }
}
