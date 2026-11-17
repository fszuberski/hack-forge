package com.fszuberski.transfer;

import com.fszuberski.transfer.testing.data.PostgresApplicationData;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StarterApplicationTest extends PostgresApplicationData {
    @Nested
    class RunnableTransfer {
        @ParameterizedTest
        @CsvSource({"USD,10.00,false", "EUR,0.01,true"})
        void shouldPersistReceiptAndBalancesGivenValidTransfer(String currency, String amount, boolean decimalString)
                throws Exception {
            // given: distinct committed accounts and exact money encoded as a JSON number or decimal string
            var source = account("100.00", currency);
            var target = account("25.00", currency);
            Object wireAmount = decimalString ? amount : new BigDecimal(amount);

            // when: a transfer crosses the real HTTP, use-case and JPA boundaries
            var response = transfer(source, target, wireAmount, currency, key());

            // then: the happy path commits balances and a receipt, without claiming ledger safety
            assertThat(response.getResponse().getStatus()).isEqualTo(200);
            var receipt = body(response);
            var id = UUID.fromString(receipt.get("transactionId").asText());
            assertThat(receipt.get("sourceAccountId").asText()).isEqualTo(source.toString());
            assertThat(receipt.get("targetAccountId").asText()).isEqualTo(target.toString());
            assertThat(receipt.get("currency").asText()).isEqualTo(currency);
            assertThat(receipt.get("amount").decimalValue()).isEqualByComparingTo(amount);
            assertThat(balance(source)).isEqualByComparingTo(new BigDecimal("100").subtract(new BigDecimal(amount)));
            assertThat(balance(target)).isEqualByComparingTo(new BigDecimal("25").add(new BigDecimal(amount)));
            assertThat(receipt.get("sourceBalance").decimalValue()).isEqualByComparingTo(balance(source));
            assertThat(receipt.get("targetBalance").decimalValue()).isEqualByComparingTo(balance(target));
            assertThat(jdbc.queryForObject("SELECT count(*) FROM transfers WHERE id = ?", Integer.class, id)).isEqualTo(1);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1.00", "0.001"})
        void shouldRejectInvalidMoneyGivenNonpositiveOrExcessScale(String amount) throws Exception {
            // given: valid committed accounts but an invalid amount
            var source = account("100.00", "USD");
            var target = account("25.00", "USD");

            // when: the request fails the starter's minimal structural validation
            var response = transfer(source, target, amount, "USD", key());

            // then: rejection happens before any committed changes
            assertThat(response.getResponse().getStatus()).isEqualTo(400);
            assertThat(balance(source)).isEqualByComparingTo("100");
            assertThat(balance(target)).isEqualByComparingTo("25");
            assertThat(transfers(source)).isZero();
        }
    }
}
