package com.binaryigor.modularmonolith.budget;

import com.binaryigor.modularmonolith.contracts.BudgetSavedEvent;
import com.binaryigor.modularmonolith.contracts.ErrorResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

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
    TestRestTemplate restTemplate;
    @Autowired
    TestBudgetSavedListener budgetSavedListener;

    @Test
    void shouldCreateAndReturnBudget() {
        var id = UUID.randomUUID();

        Assertions.assertThat(getBudget(id, ErrorResponse.class))
                .matches(r -> r.getStatusCode().equals(HttpStatus.NOT_FOUND)
                        && r.getBody().equals(ErrorResponse.fromException(new BudgetNotFoundException(id))));

        Assertions.assertThat(budgetSavedListener.capturedEvent()).isNull();

        var budget = new Budget(id, new BigDecimal("10.55"),
                Instant.now().truncatedTo(ChronoUnit.MILLIS));

        Assertions.assertThat(saveBudget(budget))
                .matches(r -> r.getStatusCode().is2xxSuccessful());

        Assertions.assertThat(getBudget(id))
                .matches(r -> r.getStatusCode().is2xxSuccessful()
                        && r.getBody().equals(budget));

        Assertions.assertThat(budgetSavedListener.capturedEvent())
                .isEqualTo(new BudgetSavedEvent(budget.id()));
    }

    private <T> ResponseEntity<T> getBudget(UUID id, Class<T> response) {
        return restTemplate.getForEntity("/budgets/" + id, response);
    }

    private ResponseEntity<Budget> getBudget(UUID id) {
        return getBudget(id, Budget.class);
    }

    private ResponseEntity<Void> saveBudget(Budget budget) {
        return restTemplate.exchange(RequestEntity.put("/budgets").body(budget), Void.class);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        TestBudgetSavedListener testBudgetSavedListener() {
            return new TestBudgetSavedListener();
        }

    }

    static class TestBudgetSavedListener {

        private final AtomicReference<BudgetSavedEvent> capturedEvent = new AtomicReference<>();

        @EventListener
        void onBudgetSaved(BudgetSavedEvent event) {
            capturedEvent.set(event);
        }

        public BudgetSavedEvent capturedEvent() {
            return capturedEvent.get();
        }
    }
}
