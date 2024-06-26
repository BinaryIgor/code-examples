package com.binaryigor.modularpattern.project;

import com.binaryigor.modularpattern.shared.events.InMemoryAppEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles(value = {"project", "integration"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = IntegrationTest.TestConfig.class)
@AutoConfigureWireMock(port = 0)
public abstract class IntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16");

    static {
        POSTGRESQL_CONTAINER.start();
        System.setProperty("PROJECT_DB_URL", POSTGRESQL_CONTAINER.getJdbcUrl());
        System.setProperty("PROJECT_DB_USERNAME", POSTGRESQL_CONTAINER.getUsername());
        System.setProperty("PROJECT_DB_PASSWORD", POSTGRESQL_CONTAINER.getPassword());
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected TestUserClient userClient;

    @Autowired
    protected InMemoryAppEvents appEvents;

    @Autowired
    protected JdbcClient jdbcClient;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userClient.clear();
        jdbcClient.sql("""
            TRUNCATE TABLE project CASCADE;
            TRUNCATE TABLE "user" CASCADE;
            """).update();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        TestUserClient userClient() {
            return new TestUserClient();
        }
    }
}
