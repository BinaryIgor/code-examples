package com.binaryigor.modularpattern.user;

import com.binaryigor.modularpattern.shared.events.AppEvents;
import com.binaryigor.modularpattern.shared.events.AppEventsPublisher;
import com.binaryigor.modularpattern.shared.outbox.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles(value = {"user", "integration"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
public abstract class IntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16");

    static {
        POSTGRESQL_CONTAINER.start();
        System.setProperty("USER_DB_URL", POSTGRESQL_CONTAINER.getJdbcUrl());
        System.setProperty("USER_DB_USERNAME", POSTGRESQL_CONTAINER.getUsername());
        System.setProperty("USER_DB_PASSWORD", POSTGRESQL_CONTAINER.getPassword());
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcClient jdbcClient;

    @Autowired
    protected OutboxRepository userOutboxRepository;

    @Autowired
    protected AppEvents appEvents;

    @Autowired
    protected AppEventsPublisher appEventsPublisher;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        jdbcClient.sql("""
            TRUNCATE "user" CASCADE;
            TRUNCATE outbox_message;
            """).update();
    }

    @TestConfiguration
    static class TestConfig {


    }
}
