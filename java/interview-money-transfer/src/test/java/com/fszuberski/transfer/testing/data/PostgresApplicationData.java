package com.fszuberski.transfer.testing.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class PostgresApplicationData {
    // One JVM-scoped disposable database, reclaimed by Ryuk on worker exit.
    // No reuse and no skip-on-missing-Docker fallback.
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withCommand("postgres", "-c", "statement_timeout=5000", "-c", "lock_timeout=2000");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "8");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "5000");
    }

    @Autowired protected MockMvc mvc;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected ObjectMapper json;

    protected UUID account(String balance, String currency) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO accounts(id, balance, currency) VALUES (?, ?, ?)",
                id, new BigDecimal(balance), currency);
        return id;
    }

    protected String key() {
        return UUID.randomUUID().toString();
    }

    protected BigDecimal balance(UUID id) {
        return jdbc.queryForObject("SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, id);
    }

    protected int transfers(UUID source) {
        return jdbc.queryForObject("SELECT count(*) FROM transfers WHERE source_account_id = ?", Integer.class, source);
    }

    protected MvcResult transfer(UUID source, UUID target, Object amount, String currency, String key) throws Exception {
        var body = new LinkedHashMap<String, Object>();
        body.put("sourceAccountId", source);
        body.put("targetAccountId", target);
        body.put("amount", amount);
        body.put("currency", currency);
        var request = post("/transfers").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body));
        if (key != null) {
            request.header("Idempotency-Key", key);
        }
        return mvc.perform(request).andReturn();
    }

    protected JsonNode body(MvcResult result) throws Exception {
        return json.reader().with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .readTree(result.getResponse().getContentAsString());
    }

    protected void assertSameReceipt(MvcResult actual, MvcResult original) throws Exception {
        var received = body(actual);
        var expected = body(original);
        for (var field : new String[]{"transactionId", "sourceAccountId", "targetAccountId", "currency"}) {
            assertThat(received.required(field).asText()).as(field).isEqualTo(expected.required(field).asText());
        }
        for (var field : new String[]{"amount", "sourceBalance", "targetBalance"}) {
            assertThat(received.required(field).decimalValue()).as(field)
                    .isEqualByComparingTo(expected.required(field).decimalValue());
        }
    }
}
