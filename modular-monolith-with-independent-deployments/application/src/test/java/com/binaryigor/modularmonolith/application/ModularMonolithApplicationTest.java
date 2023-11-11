package com.binaryigor.modularmonolith.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ModularMonolithApplicationTest {

    static final PostgreSQLContainer<?> BUDGET_POSTGRESQL_CONTAINER = startDbContainerAndSetupProperties("BUDGET");
    static final PostgreSQLContainer<?> CAMPAIGN_POSTGRESQL_CONTAINER = startDbContainerAndSetupProperties("CAMPAIGN");
    static final PostgreSQLContainer<?> INVENTORY_POSTGRESQL_CONTAINER = startDbContainerAndSetupProperties("INVENTORY");

    private static PostgreSQLContainer<?> startDbContainerAndSetupProperties(String propertiesPrefix) {
        var dbContainer = new PostgreSQLContainer<>("postgres:15");

        dbContainer.start();

        System.setProperty(propertiesPrefix + "_DB_URL", dbContainer.getJdbcUrl());
        System.setProperty(propertiesPrefix + "_DB_USERNAME", dbContainer.getUsername());
        System.setProperty(propertiesPrefix + "_DB_PASSWORD", dbContainer.getPassword());

        return dbContainer;
    }

    @Test
    void shouldStartTheApplication() {
        System.out.println("Should start the application!");
    }
}
