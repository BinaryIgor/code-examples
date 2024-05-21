package com.binaryigor.modularpattern.monolith;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ModularMonolithApplicationTest {

    static final PostgreSQLContainer<?> USER_POSTGRESQL_CONTAINER = startDbContainerAndSetupProperties("USER");

    private static PostgreSQLContainer<?> startDbContainerAndSetupProperties(String propertiesPrefix) {
        var dbContainer = new PostgreSQLContainer<>("postgres:16");

        dbContainer.start();

        System.setProperty(propertiesPrefix + "_DB_URL", dbContainer.getJdbcUrl());
        System.setProperty(propertiesPrefix + "_DB_USERNAME", dbContainer.getUsername());
        System.setProperty(propertiesPrefix + "_DB_PASSWORD", dbContainer.getPassword());

        return dbContainer;
    }

    @Test
    void startsTheApplication() {
        System.out.println("Starts the application!");
    }
}
