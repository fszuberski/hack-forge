package com.fszuberski.transfer;

import com.fszuberski.transfer.testing.data.PostgresApplicationData;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Tag("interview")
class TransferAcceptanceTest extends PostgresApplicationData {
    @Nested
    class LedgerAndAtomicity {
        @Test
        void shouldPostBalancedHistoryGivenSuccessfulTransfer() throws Exception {
            // given: committed opening balances, not yet represented by transfer postings
            var source = account("100.00", "USD");
            var target = account("25.00", "USD");

            // when: ten dollars moves between the accounts
            var response = transfer(source, target, "10.00", "USD", key());

            // then: exactly two signed entries explain the receipt and committed balances
            assertThat(response.getResponse().getStatus()).isEqualTo(200);
            var receipt = body(response);
            var transaction = UUID.fromString(receipt.get("transactionId").asText());
            var rows = jdbc.queryForList("""
                    SELECT account_id, amount, currency, balance_after
                    FROM ledger_entries WHERE transaction_id = ? ORDER BY amount
                    """, transaction);
            assertThat(rows).hasSize(2);
            assertThat(rows.getFirst().get("account_id")).isEqualTo(source);
            assertThat(rows.getLast().get("account_id")).isEqualTo(target);
            assertThat((BigDecimal) rows.getFirst().get("amount")).isEqualByComparingTo("-10");
            assertThat((BigDecimal) rows.getLast().get("amount")).isEqualByComparingTo("10");
            assertThat(rows).allSatisfy(row -> assertThat(row.get("currency")).isEqualTo("USD"));
            assertThat((BigDecimal) rows.getFirst().get("balance_after")).isEqualByComparingTo(balance(source));
            assertThat((BigDecimal) rows.getLast().get("balance_after")).isEqualByComparingTo(balance(target));
            assertThat(receipt.get("sourceBalance").decimalValue()).isEqualByComparingTo(balance(source));
            assertThat(receipt.get("targetBalance").decimalValue()).isEqualByComparingTo(balance(target));
        }

        @ParameterizedTest
        @ValueSource(strings = {"source", "target"})
        void shouldLeaveNoEffectsGivenMissingAccount(String missing) throws Exception {
            // given: one real account and one nonexistent account, scoped to this scenario
            var existing = account("100.00", "USD");
            var absent = UUID.randomUUID();
            var source = missing.equals("source") ? absent : existing;
            var target = missing.equals("target") ? absent : existing;

            // when: the requested transfer cannot resolve both accounts
            var response = transfer(source, target, "10.00", "USD", key());

            // then: neither rejection direction changes committed state
            assertAll(
                    () -> assertThat(response.getResponse().getStatus()).isEqualTo(404),
                    () -> assertThat(balance(existing)).isEqualByComparingTo("100"),
                    () -> assertThat(transfers(source)).isZero(),
                    () -> assertThat(jdbc.queryForObject(
                            "SELECT count(*) FROM ledger_entries WHERE account_id = ?", Integer.class, existing)).isZero());
        }
    }

    @Nested
    class BusinessRejections {
        @ParameterizedTest
        @CsvSource({"insufficient,409", "self,400", "mixed,400", "mismatch,400"})
        void shouldRejectWithoutMutationGivenInvalidBusinessRequest(String scenario, int expected) throws Exception {
            // given: a request violating one documented business rule, with otherwise valid money
            var source = account("100.00", "USD");
            var target = scenario.equals("self") ? source : account("25.00", scenario.equals("mixed") ? "EUR" : "USD");
            String amount = scenario.equals("insufficient") ? "100.01" : "10.00";
            String currency = scenario.equals("mismatch") ? "EUR" : "USD";
            var beforeTarget = balance(target);

            // when: the invalid business request enters the real application
            var response = transfer(source, target, amount, currency, key());

            // then: the documented rejection leaves balances, history and postings untouched
            assertAll(
                    () -> assertThat(response.getResponse().getStatus()).isEqualTo(expected),
                    () -> assertThat(balance(source)).isEqualByComparingTo("100"),
                    () -> assertThat(balance(target)).isEqualByComparingTo(beforeTarget),
                    () -> assertThat(transfers(source)).isZero());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void shouldRejectWithoutMutationGivenMissingKey(String key) throws Exception {
            // given: valid accounts and money but no usable idempotency identity
            var source = account("100.00", "USD");
            var target = account("25.00", "USD");

            // when: a request omits the key or supplies only whitespace
            var response = transfer(source, target, "10.00", "USD", key);

            // then: bad identity is a 400 before any money moves
            assertAll(
                    () -> assertThat(response.getResponse().getStatus()).isEqualTo(400),
                    () -> assertThat(balance(source)).isEqualByComparingTo("100"),
                    () -> assertThat(balance(target)).isEqualByComparingTo("25"),
                    () -> assertThat(transfers(source)).isZero());
        }
    }

    @Nested
    class Idempotency {
        @ParameterizedTest
        @CsvSource({"10.00,10.00", "10,10.00"})
        void shouldReturnOriginalReceiptGivenNormalizedKeyAndEquivalentPayload(String firstAmount, String replayAmount)
                throws Exception {
            // given: one successful transfer whose key has surrounding whitespace
            var source = account("100.00", "USD");
            var target = account("25.00", "USD");
            var key = key();
            var first = transfer(source, target, firstAmount, "USD", "  " + key + "  ");
            assertThat(first.getResponse().getStatus()).isEqualTo(200);

            // when: the normalized identity and numerically equal payload are replayed
            var replay = transfer(source, target, replayAmount, "USD", key);

            // then: the original response is returned with no extra committed mutation
            assertAll(
                    () -> assertThat(replay.getResponse().getStatus()).isEqualTo(200),
                    () -> assertSameReceipt(replay, first),
                    () -> assertThat(transfers(source)).isEqualTo(1),
                    () -> assertThat(balance(source)).isEqualByComparingTo("90"),
                    () -> assertThat(balance(target)).isEqualByComparingTo("35"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"amount", "target"})
        void shouldConflictWithoutMutationGivenSameKeyAndDifferentPayload(String changed) throws Exception {
            // given: one committed transfer and another valid destination in the same currency
            var source = account("100.00", "USD");
            var target = account("25.00", "USD");
            var other = account("0.00", "USD");
            var key = key();
            var first = transfer(source, target, "10.00", "USD", key);
            assertThat(first.getResponse().getStatus()).isEqualTo(200);

            // when: an existing key is reused with a changed amount or target
            var replay = transfer(source, changed.equals("target") ? other : target,
                    changed.equals("amount") ? "11.00" : "10.00", "USD", key);

            // then: a conflict preserves the first operation and all subsequent balances
            assertAll(
                    () -> assertThat(replay.getResponse().getStatus()).isEqualTo(409),
                    () -> assertThat(transfers(source)).isEqualTo(1),
                    () -> assertThat(balance(source)).isEqualByComparingTo("90"),
                    () -> assertThat(balance(target)).isEqualByComparingTo("35"),
                    () -> assertThat(balance(other)).isEqualByComparingTo("0"));
        }

        @Test
        void shouldExecuteTwiceGivenDistinctCaseSensitiveKeys() throws Exception {
            // given: identical payloads with keys differing only by case in a unique namespace
            var source = account("100.00", "USD");
            var target = account("25.00", "USD");
            var key = "case-" + key();

            // when: two independent identities request the same movement
            var first = transfer(source, target, "10.00", "USD", key + "a");
            var second = transfer(source, target, "10.00", "USD", key + "A");

            // then: both movements commit; this alone says nothing about replay safety
            assertThat(first.getResponse().getStatus()).isEqualTo(200);
            assertThat(second.getResponse().getStatus()).isEqualTo(200);
            assertThat(body(first).get("transactionId")).isNotEqualTo(body(second).get("transactionId"));
            assertThat(transfers(source)).isEqualTo(2);
            assertThat(balance(source)).isEqualByComparingTo("80");
            assertThat(balance(target)).isEqualByComparingTo("45");
        }
    }
}
