package com.fszuberski.multithreading.contention;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;

import static com.fszuberski.multithreading.support.TestThreads.raceIndexed;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class DeadlockFreeLedgerTest {
    @Test
    void shouldTerminateAndPreserveBalancesGivenOpposingTransfers() throws Exception {
        var ledger = new DeadlockFreeLedger(2, 10_000);
        raceIndexed(8, index -> () -> {
            int from = index % 2;
            for (int i = 0; i < 1_000; i++) {
                ledger.transfer(from, 1 - from, 1);
            }
            return null;
        });
        var balances = ledger.balances();
        assertEquals(20_000, Arrays.stream(balances).sum());
        assertTrue(Arrays.stream(balances).allMatch(balance -> balance >= 0));
        balances[0] = -1;
        assertTrue(ledger.balances()[0] >= 0, "Do not expose mutable internal balances");
    }

    @Test
    void shouldLeaveBalancesUnchangedGivenSelfTransferOrInsufficientFunds() throws Exception {
        var ledger = new DeadlockFreeLedger(2, 10);
        assertTrue(ledger.transfer(0, 0, 5));
        assertFalse(ledger.transfer(0, 1, 11));
        assertArrayEquals(new long[]{10, 10}, ledger.balances());
        assertThrows(IllegalArgumentException.class, () -> ledger.transfer(0, 1, -1));
    }
}
