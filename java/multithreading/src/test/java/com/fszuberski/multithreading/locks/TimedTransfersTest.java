package com.fszuberski.multithreading.locks;

import com.fszuberski.multithreading.support.TestThreads;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class TimedTransfersTest {
    @Test
    void shouldTransferAtomicallyAndRejectOverdraftGivenAccountBalances() throws Exception {
        var transfers = new TimedTransfers();
        var a = new TimedTransfers.Account(100);
        var b = new TimedTransfers.Account(40);
        assertTrue(transfers.transfer(a, b, 30, WAIT));
        assertEquals(70, a.balance());
        assertEquals(70, b.balance());
        assertFalse(transfers.transfer(a, b, 100, WAIT));
        assertEquals(140, a.balance() + b.balance());
        assertThrows(IllegalArgumentException.class, () -> transfers.transfer(a, b, 0, WAIT));
    }

    @Test
    void shouldReleaseFirstLockAndPreserveBalancesGivenSecondLockTimeout() throws Exception {
        var transfers = new TimedTransfers();
        var a = new TimedTransfers.Account(100);
        var b = new TimedTransfers.Account(100);
        try (var threads = new TestThreads(1)) {
            b.lock.lock();
            try {
                assertFalse(result(threads.submit(() -> transfers.transfer(a, b, 10, Duration.ofMillis(50)))));
                assertTrue(result(threads.submit(() -> {
                    boolean acquired = a.lock.tryLock();
                    if (acquired) {
                        a.lock.unlock();
                    }
                    return acquired;
                })));
            } finally {
                b.lock.unlock();
            }
        }
        assertEquals(100, a.balance());
        assertEquals(100, b.balance());
    }
}
