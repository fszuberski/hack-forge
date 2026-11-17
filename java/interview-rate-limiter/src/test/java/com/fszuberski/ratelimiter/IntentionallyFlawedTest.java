package com.fszuberski.ratelimiter;

import com.fszuberski.ratelimiter.testing.data.InterviewFixtureData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * INTENTIONALLY FLAWED diagnosis exercises, NOT acceptance evidence.
 * Both assumptions contradict the README contract. Repair them; do not change production to match.
 */
@Tag("interview")
class IntentionallyFlawedTest {
    @Test
    void shouldRejectOtherClientGivenFirstClientAtCapacity() {
        // given: one client has consumed a capacity-one budget at frozen time
        var data = new InterviewFixtureData(1);
        data.limiter.acquire("client-a");

        // when: an unrelated client requests its first permit
        var otherClient = data.limiter.acquire("client-b");

        // then: INTENTIONAL DEFECT — assumes that the budget is global rather than per client
        assertFalse(otherClient.allowed());
    }

    @Test
    void shouldResetQuotaGivenAnotherEndpoint() throws Exception {
        // given: one client has consumed its only permit on the greeting endpoint
        var mvc = new InterviewFixtureData(1).mvc();
        mvc.perform(get("/api/greeting").header("X-API-Key", "client-a")).andExpect(status().isOk());

        // when: the same client switches to another API resource without advancing time
        var response = mvc.perform(get("/api/items").header("X-API-Key", "client-a"));

        // then: INTENTIONAL DEFECT — assumes each endpoint owns a separate client budget
        response.andExpect(status().isOk());
    }
}
