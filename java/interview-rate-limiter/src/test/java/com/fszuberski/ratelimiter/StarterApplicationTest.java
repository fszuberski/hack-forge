package com.fszuberski.ratelimiter;

import com.fszuberski.ratelimiter.adapter.http.RateLimitFilter;
import com.fszuberski.ratelimiter.core.LimiterSettings;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StarterApplicationTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private Clock clock;
    @Autowired
    private LimiterSettings settings;
    @Autowired
    private RateLimitFilter filter;

    @Nested
    class RunnableEndpoints {
        @ParameterizedTest
        @ValueSource(strings = {"/api/greeting", "/api/items"})
        void shouldServeJsonGivenFreshClient(String path) throws Exception {
            // given: a distinct client with untouched quota in the real Boot application
            String key = UUID.randomUUID().toString();

            // when: the client requests a demo resource through the registered filters
            var response = mvc.perform(get(path).header("X-API-Key", key));

            // then: the runnable starter serves JSON
            response.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        void shouldEchoBodyGivenFreshClient() throws Exception {
            // given: a distinct client and a JSON message
            String key = UUID.randomUUID().toString();
            String body = "{\"message\":\"interview\"}";

            // when: the message is posted through the real application wiring
            var response = mvc.perform(post("/api/echo").header("X-API-Key", key)
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            // then: the demo returns the same JSON message
            response.andExpect(status().isOk()).andExpect(content().json(body));
        }

        @Test
        void shouldReportHealthGivenNoApiKey() throws Exception {
            // given: a health probe without client identity
            var request = get("/health");

            // when: the probe crosses the real application's filter registration
            var response = mvc.perform(request);

            // then: health is public and available
            response.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        }
    }

    @Test
    void shouldExposeUtcSeamGivenDefaultConfiguration() {
        // given: the application has started with its default bean configuration
        var expected = new LimiterSettings(5, Duration.ofSeconds(10), Duration.ofMinutes(5));

        // when: the configured time zone and limiter settings are inspected
        var zone = clock.getZone();

        // then: UTC time, documented settings, and the unfinished filter are wired
        assertEquals(ZoneOffset.UTC, zone);
        assertEquals(expected, settings);
        assertNotNull(filter);
    }
}
