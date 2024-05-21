package com.binaryigor.modularpattern.user;

import com.binaryigor.modularpattern.shared.outbox.OutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles(value = {"user", "integration"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16");

    static {
        POSTGRESQL_CONTAINER.start();
        System.setProperty("USER_DB_URL", POSTGRESQL_CONTAINER.getJdbcUrl());
        System.setProperty("USER_DB_USERNAME", POSTGRESQL_CONTAINER.getUsername());
        System.setProperty("USER_DB_PASSWORD", POSTGRESQL_CONTAINER.getPassword());
    }

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    OutboxRepository userOutboxRepository;

    @TestConfiguration
    static class TestConfig {


    }
}
