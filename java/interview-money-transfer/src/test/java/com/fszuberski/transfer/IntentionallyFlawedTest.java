package com.fszuberski.transfer;

import com.fszuberski.transfer.testing.data.PostgresApplicationData;
import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Diagnosis exercises, NOT valid acceptance criteria. Repair the tests, not the contract. */
@Tag("interview")
class IntentionallyFlawedTest extends PostgresApplicationData {
    @Nested
    class RepairTheseAssumptions {
        @Test
        void shouldMatchMoneyGivenDifferentDecimalScales() throws Exception {
            // given: a money expectation numerically equal to the future balance but with different scale
            var source = account("100.00", "USD");
            var target = account("25.00", "USD");
            var expected = new BigDecimal("90.0");

            // when: the database commits a ten-dollar transfer
            var response = transfer(source, target, "10.00", "USD", key());

            // then: INTENTIONALLY FLAWED: BigDecimal.equals compares representation, not just money value
            assertThat(response.getResponse().getStatus()).isEqualTo(200);
            assertEquals(expected, balance(source));
        }

        @Test
        void shouldDebitTwiceGivenSameKeyReplay() throws Exception {
            // given: a unique key identifying one operation, not two independent requests
            var source = account("100.00", "USD");
            var target = account("25.00", "USD");
            var key = key();

            // when: the exact same operation is sent twice
            var first = transfer(source, target, "10.00", "USD", key);
            var replay = transfer(source, target, "10.00", "USD", key);

            // then: INTENTIONALLY FLAWED false confidence: these pass the starter but violate replay semantics
            assertThat(first.getResponse().getStatus()).isEqualTo(200);
            assertThat(replay.getResponse().getStatus()).isEqualTo(200);
            assertThat(balance(source)).isEqualByComparingTo("80");
            assertThat(transfers(source)).isEqualTo(2);
        }
    }
}
