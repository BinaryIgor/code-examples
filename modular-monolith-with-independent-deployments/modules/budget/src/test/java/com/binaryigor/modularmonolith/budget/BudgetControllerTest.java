package com.binaryigor.modularmonolith.budget;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@ActiveProfiles(value = {"budget", "integration"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BudgetControllerTest {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15");

    static {
        POSTGRESQL_CONTAINER.start();
        System.setProperty("BUDGET_DB_URL", POSTGRESQL_CONTAINER.getJdbcUrl());
        System.setProperty("BUDGET_DB_USERNAME", POSTGRESQL_CONTAINER.getUsername());
        System.setProperty("BUDGET_DB_PASSWORD", POSTGRESQL_CONTAINER.getPassword());
    }

    @Autowired
    BudgetController controller;

    @Test
    void shouldCreateAndReturnBudget() {
        var id = UUID.randomUUID();

        Assertions.assertThatThrownBy(() -> controller.get(id))
                .isInstanceOf(BudgetNotFoundException.class)
                .hasMessageContaining(id.toString());

        var budget = new Budget(id, new BigDecimal("10.55"),
                Instant.now().truncatedTo(ChronoUnit.MILLIS));

        controller.save(budget);

        Assertions.assertThat(controller.get(id)).isEqualTo(budget);
    }

}