package com.binaryigor.modularpattern.shared;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class IntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16");
    protected static Connection POSTGRESQL_CONNECTION;

    static {
        POSTGRESQL_CONTAINER.start();
        try {
            POSTGRESQL_CONNECTION = DriverManager.getConnection(POSTGRESQL_CONTAINER.getJdbcUrl(),
                    POSTGRESQL_CONTAINER.getUsername(),
                    POSTGRESQL_CONTAINER.getPassword());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected JdbcClient jdbcClient() {
        var dataSource = new SingleConnectionDataSource(POSTGRESQL_CONTAINER.getJdbcUrl(),
                POSTGRESQL_CONTAINER.getUsername(), POSTGRESQL_CONTAINER.getPassword(), true);
        return JdbcClient.create(dataSource);
    }
}
