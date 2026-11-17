package com.fszuberski.ratelimiter;

import com.fszuberski.ratelimiter.core.Decision;
import com.fszuberski.ratelimiter.core.RateLimiter;
import com.fszuberski.ratelimiter.testing.data.InterviewFixtureData;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Focused servlet integration: real controller/filter/limiter except explicit decision-seam tests. */
@Tag("interview")
class HttpAcceptanceTest {
    @Nested
    class ClientIdentity {
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "   ")
        void shouldRejectBadRequestGivenMissingOrBlankKey(String key) throws Exception {
            // given: an API request without a usable identity and a fresh controlled clock
            var mvc = new InterviewFixtureData(2).mvc();
            var request = get("/api/greeting");
            if (key != null) {
                request.header("X-API-Key", key);
            }

            // when: the anonymous request crosses the real filter
            var response = mvc.perform(request);

            // then: invalid identity is a 400 JSON error, not quota exhaustion
            response.andExpect(status().isBadRequest())
                    .andExpect(content().json("{\"error\":\"missing_api_key\"}"))
                    .andExpect(header().doesNotExist("Retry-After"));
        }

        @Test
        void shouldShareQuotaGivenDifferentApiEndpoints() throws Exception {
            // given: a single client has spent two permits across different resources
            var mvc = new InterviewFixtureData(2).mvc();
            mvc.perform(get("/api/greeting").header("X-API-Key", "client-a")).andExpect(status().isOk());
            mvc.perform(get("/api/items").header("X-API-Key", "client-a")).andExpect(status().isOk());

            // when: that client returns to the first endpoint at the same instant
            var response = mvc.perform(get("/api/greeting").header("X-API-Key", "client-a"));

            // then: all API resources share one client budget
            response.andExpect(status().isTooManyRequests())
                    .andExpect(content().json("{\"error\":\"rate_limit_exceeded\"}"));
        }

        @Test
        void shouldShareQuotaGivenWhitespaceAroundSameCaseSensitiveKey() throws Exception {
            // given: a capacity-one client has spent its permit using a padded key
            var mvc = new InterviewFixtureData(1).mvc();
            mvc.perform(get("/api/items").header("X-API-Key", " Client-A ")).andExpect(status().isOk());
            mvc.perform(get("/api/items").header("X-API-Key", "client-a")).andExpect(status().isOk());

            // when: the original case-sensitive identity is sent without surrounding whitespace
            var response = mvc.perform(get("/api/items").header("X-API-Key", "Client-A"));

            // then: stripping whitespace does not create a second budget
            response.andExpect(status().isTooManyRequests());
        }
    }

    @Nested
    class DecisionMapping {
        @ParameterizedTest
        @CsvSource({"1, 1", "1000, 1", "1001, 2", "2500, 3"})
        void shouldRoundRetryUpGivenDeniedDecision(long delayMillis, int expectedSeconds) throws Exception {
            // given: the algorithm-neutral port denies with a precisely controlled remaining delay
            var limiter = new FixedDecisionLimiterData(new Decision(false, Duration.ofMillis(delayMillis)));
            var mvc = InterviewFixtureData.mvc(limiter);

            // when: a valid client requests a demo endpoint
            var response = mvc.perform(get("/api/greeting").header("X-API-Key", "client-a"));

            // then: denial replaces the controller body and exposes ceiling-rounded delta seconds
            response.andExpect(status().isTooManyRequests())
                    .andExpect(header().string("Retry-After", Integer.toString(expectedSeconds)))
                    .andExpect(content().contentTypeCompatibleWith("application/json"))
                    .andExpect(content().json("{\"error\":\"rate_limit_exceeded\"}"));
            assertEquals(1, limiter.calls.get());
        }

        @Test
        void shouldBypassLimiterGivenHealthProbeAndDenyingPort() throws Exception {
            // given: the decision seam would deny every attempt, even for an identified client
            var limiter = new FixedDecisionLimiterData(new Decision(false, Duration.ofSeconds(1)));
            var mvc = InterviewFixtureData.mvc(limiter);

            // when: an anonymous health probe crosses the same filter
            var response = mvc.perform(get("/health"));

            // then: health stays available and consumes no permit
            response.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"))
                    .andExpect(header().doesNotExist("Retry-After"));
            assertEquals(0, limiter.calls.get());
        }

        @Test
        void shouldOmitRetryHeaderGivenAvailablePermit() throws Exception {
            // given: a fresh real limiter with a per-scenario frozen clock
            var mvc = new InterviewFixtureData(1).mvc();

            // when: a valid client uses its first permit
            var response = mvc.perform(get("/api/items").header("X-API-Key", "client-a"));

            // then: successful controller responses are unchanged and carry no retry instruction
            response.andExpect(status().isOk()).andExpect(header().doesNotExist("Retry-After"))
                    .andExpect(content().json("[\"book\",\"pen\"]"));
        }
    }

    /** A port input for HTTP translation only, not a replacement for algorithm acceptance coverage. */
    private static final class FixedDecisionLimiterData implements RateLimiter {
        private final Decision decision;
        private final AtomicInteger calls = new AtomicInteger();

        private FixedDecisionLimiterData(Decision decision) {
            this.decision = decision;
        }

        @Override
        public Decision acquire(String clientKey) {
            calls.incrementAndGet();
            return decision;
        }

        @Override
        public int cleanup() {
            return 0;
        }
    }
}
