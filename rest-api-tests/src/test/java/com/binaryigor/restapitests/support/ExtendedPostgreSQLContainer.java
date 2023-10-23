package com.binaryigor.restapitests.support;

import org.testcontainers.containers.PostgreSQLContainer;

public class ExtendedPostgreSQLContainer extends PostgreSQLContainer<ExtendedPostgreSQLContainer> {

    private static ExtendedPostgreSQLContainer instance;

    private ExtendedPostgreSQLContainer() {
        super("postgres:15");
    }

    public static ExtendedPostgreSQLContainer instance() {
        if (instance == null) {
            instance = new ExtendedPostgreSQLContainer();
            instance.start();
        }
        return instance;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", instance.getJdbcUrl());
        System.setProperty("DB_USERNAME", instance.getUsername());
        System.setProperty("DB_PASSWORD", instance.getPassword());
    }
}
