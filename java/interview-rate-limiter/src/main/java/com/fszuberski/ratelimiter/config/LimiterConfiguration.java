package com.fszuberski.ratelimiter.config;

import com.fszuberski.ratelimiter.adapter.http.RateLimitFilter;
import com.fszuberski.ratelimiter.core.InMemoryRateLimiter;
import com.fszuberski.ratelimiter.core.LimiterSettings;
import com.fszuberski.ratelimiter.core.RateLimiter;
import java.time.Clock;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LimiterConfiguration {
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    LimiterSettings limiterSettings() {
        return new LimiterSettings(5, Duration.ofSeconds(10), Duration.ofMinutes(5));
    }

    @Bean
    RateLimiter rateLimiter(LimiterSettings settings, Clock clock) {
        return new InMemoryRateLimiter(settings, clock);
    }

    @Bean
    RateLimitFilter rateLimitFilter(RateLimiter rateLimiter) {
        return new RateLimitFilter(rateLimiter);
    }
}
