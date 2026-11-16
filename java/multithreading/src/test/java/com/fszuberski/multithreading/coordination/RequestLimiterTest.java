package com.fszuberski.multithreading.coordination;

import com.fszuberski.multithreading.support.TestThreads;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static com.fszuberski.multithreading.support.TestThreads.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class RequestLimiterTest {
    @Test
    void shouldBoundActiveCallsAndReturnPermitsGivenSaturatedLimiter() throws Exception {
        var limiter = new RequestLimiter(3);
        var entered = new CountDownLatch(3);
        var release = new CountDownLatch(1);
        var active = new AtomicInteger();
        var maximum = new AtomicInteger();
        try (var threads = new TestThreads(8)) {
            var futures = new ArrayList<Future<Integer>>();
            for (int i = 0; i < 8; i++) {
                futures.add(threads.submit(() -> limiter.call(() -> {
                    int count = active.incrementAndGet();
                    maximum.accumulateAndGet(count, Math::max);
                    entered.countDown();
                    try {
                        await(release);
                        return 1;
                    } finally {
                        active.decrementAndGet();
                    }
                })));
            }
            try {
                await(entered);
                assertEquals(3, maximum.get());
                assertEquals(0, limiter.availablePermits());
            } finally {
                release.countDown();
            }
            for (var future : futures) {
                assertEquals(1, result(future));
            }
        }
        assertEquals(3, maximum.get());
        assertEquals(3, limiter.availablePermits());
    }

    @Test
    void shouldReturnPermitGivenCallbackFailure() throws Exception {
        var limiter = new RequestLimiter(1);
        assertThrows(IllegalArgumentException.class, () -> limiter.call(() -> {
            throw new IllegalArgumentException("downstream failed");
        }));
        assertEquals(1, limiter.availablePermits());
        assertEquals("ok", limiter.call(() -> "ok"));
    }

    @Test
    void shouldPreservePermitCountGivenInterruptedAcquisition() throws Exception {
        var limiter = new RequestLimiter(1);
        var holding = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var waiting = new CountDownLatch(1);
        var propagated = new CountDownLatch(1);
        try (var threads = new TestThreads(2)) {
            var owner = threads.submit(() -> limiter.call(() -> {
                holding.countDown();
                await(release);
                return null;
            }));
            try {
                await(holding);
                var waiter = threads.submit(() -> {
                    waiting.countDown();
                    try {
                        limiter.call(() -> {
                            fail("Interrupted waiter must not enter the action");
                            return null;
                        });
                    } catch (InterruptedException expected) {
                        propagated.countDown();
                    }
                    return null;
                });
                try {
                    await(waiting);
                } finally {
                    waiter.cancel(true);
                }
                await(propagated);
                assertEquals(0, limiter.availablePermits());
            } finally {
                release.countDown();
            }
            result(owner);
            assertEquals(1, limiter.availablePermits());
        }
    }
}
