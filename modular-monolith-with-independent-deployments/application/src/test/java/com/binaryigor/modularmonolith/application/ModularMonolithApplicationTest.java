package com.binaryigor.modularmonolith.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ModularMonolithApplicationTest.TestConfig.class)
public class ModularMonolithApplicationTest {

    static final PostgreSQLContainer<?> BUDGET_POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15");
    static final PostgreSQLContainer<?> CAMPAIGN_POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15");
    static final PostgreSQLContainer<?> INVENTORY_POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15");

    static {
        startDbContainerAndSetupProperties(BUDGET_POSTGRESQL_CONTAINER, "BUDGET");
        startDbContainerAndSetupProperties(CAMPAIGN_POSTGRESQL_CONTAINER, "CAMPAIGN");
        startDbContainerAndSetupProperties(INVENTORY_POSTGRESQL_CONTAINER, "INVENTORY");
    }

    private static void startDbContainerAndSetupProperties(PostgreSQLContainer<?> dbContainer,
                                                           String propertiesPrefix) {
        dbContainer.start();
        System.setProperty(propertiesPrefix + "_DB_URL", dbContainer.getJdbcUrl());
        System.setProperty(propertiesPrefix + "_DB_USERNAME", dbContainer.getUsername());
        System.setProperty(propertiesPrefix + "_DB_PASSWORD", dbContainer.getPassword());
    }

    @Test
    void shouldStartTheApplication() {
        System.out.println("Should start the application!");
    }

    @TestConfiguration
    static class TestConfig {

    }
}
