package com.fszuberski.ratelimiter.testing.data;

import com.fszuberski.ratelimiter.adapter.http.DemoController;
import com.fszuberski.ratelimiter.adapter.http.RateLimitFilter;
import com.fszuberski.ratelimiter.core.InMemoryRateLimiter;
import com.fszuberski.ratelimiter.core.LimiterSettings;
import com.fszuberski.ratelimiter.core.RateLimiter;
import java.time.Duration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public final class InterviewFixtureData {
    public final MutableClockData clock = new MutableClockData();
    public final LimiterSettings settings;
    public final RateLimiter limiter;

    public InterviewFixtureData(int capacity) {
        settings = new LimiterSettings(capacity, Duration.ofSeconds(10), Duration.ofMinutes(5));
        limiter = new InMemoryRateLimiter(settings, clock);
    }

    public MockMvc mvc() {
        return mvc(limiter);
    }

    public static MockMvc mvc(RateLimiter limiter) {
        return MockMvcBuilders.standaloneSetup(new DemoController())
                .addFilters(new RateLimitFilter(limiter)).build();
    }
}
