package com.binaryigor.modularpattern.shared;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

public abstract class IntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16");

    static {
        POSTGRESQL_CONTAINER.start();
    }

    protected final DataSource dataSource = new DriverManagerDataSource(POSTGRESQL_CONTAINER.getJdbcUrl(),
        POSTGRESQL_CONTAINER.getUsername(), POSTGRESQL_CONTAINER.getPassword());
    protected final JdbcClient jdbcClient = JdbcClient.create(dataSource);
}
