package com.fszuberski.ratelimiter;

import com.fszuberski.ratelimiter.testing.data.InterviewFixtureData;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Valid acceptance tests; failures here mean missing implementation, not broken assertions. */
@Tag("interview")
class LimiterAcceptanceTest {
    @Nested
    class Admission {
        @ParameterizedTest
        @ValueSource(ints = {1, 3})
        void shouldDenyCapacityPlusOneGivenSameClientAndInstant(int capacity) {
            // given: a fresh limiter with frozen time and a full client budget
            var data = new InterviewFixtureData(capacity);

            // when: one client consumes its entire burst at the same instant
            for (int request = 0; request < capacity; request++) {
                assertTrue(data.limiter.acquire("client-a").allowed());
            }
            var excess = data.limiter.acquire("client-a");

            // then: exactly the extra request is denied
            assertFalse(excess.allowed());
        }

        @Test
        void shouldAdmitOtherClientGivenFirstClientAtCapacity() {
            // given: one client has consumed a capacity-one budget at frozen time
            var data = new InterviewFixtureData(1);
            assertTrue(data.limiter.acquire("client-a").allowed());

            // when: a different client requests its first permit
            var otherClient = data.limiter.acquire("client-b");

            // then: budgets are independent and admission has no retry delay
            assertTrue(otherClient.allowed());
            assertTrue(otherClient.retryAfter().isZero());
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 3})
        void shouldRestoreFullCapacityGivenWholeWindowElapsed(int capacity) {
            // given: a client has consumed its budget at the start of a frozen window
            var data = new InterviewFixtureData(capacity);
            for (int request = 0; request < capacity; request++) {
                assertTrue(data.limiter.acquire("client-a").allowed());
            }

            // when: an entire window elapses without any other requests
            data.clock.advance(data.settings.window());

            // then: a full burst is available for either permitted algorithm
            for (int request = 0; request < capacity; request++) {
                assertTrue(data.limiter.acquire("client-a").allowed());
            }
        }

        @Test
        void shouldReportPositiveBoundedDelayGivenExhaustedBudget() {
            // given: a capacity-one client has no permits at the same instant
            var data = new InterviewFixtureData(1);
            assertTrue(data.limiter.acquire("client-a").allowed());

            // when: the client immediately requests another permit
            var denied = data.limiter.acquire("client-a");

            // then: retry is positive and no later than one complete window
            assertFalse(denied.allowed());
            assertTrue(denied.retryAfter().isPositive());
            assertTrue(denied.retryAfter().compareTo(data.settings.window()) <= 0);
        }
    }

    @Nested
    class ConcurrentAdmission {
        @Test
        void shouldAdmitExactlyCapacityGivenConcurrentRequestsAtOneInstant() throws Exception {
            // given: twelve callers contend for three permits while the clock stays frozen
            var data = new InterviewFixtureData(3);
            var start = new CountDownLatch(1);
            var ready = new CountDownLatch(12);
            var executor = Executors.newFixedThreadPool(12);
            var results = new ArrayList<Future<Boolean>>();
            try {
                for (int caller = 0; caller < 12; caller++) {
                    results.add(executor.submit(() -> {
                        ready.countDown();
                        if (!start.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("Start signal timed out");
                        }
                        return data.limiter.acquire("shared-client").allowed();
                    }));
                }

                // when: all submitted callers are released and their bounded results collected
                assertTrue(ready.await(5, TimeUnit.SECONDS), "Callers did not become ready");
                start.countDown();
                int admitted = 0;
                for (var result : results) {
                    if (result.get(5, TimeUnit.SECONDS)) {
                        admitted++;
                    }
                }

                // then: concurrency cannot overspend or lose the three available permits
                assertEquals(3, admitted);
            } finally {
                start.countDown();
                results.forEach(result -> result.cancel(true));
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "Executor did not terminate");
            }
        }
    }
}
