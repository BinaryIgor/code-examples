package com.binaryigor.modularmonolith.inventory;

import com.binaryigor.modularmonolith.contracts.ErrorResponse;
import com.binaryigor.modularmonolith.contracts.InventorySavedEvent;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@ActiveProfiles(value = {"inventory", "integration"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InventoryControllerTest {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15");

    static {
        POSTGRESQL_CONTAINER.start();
        System.setProperty("INVENTORY_DB_URL", POSTGRESQL_CONTAINER.getJdbcUrl());
        System.setProperty("INVENTORY_DB_USERNAME", POSTGRESQL_CONTAINER.getUsername());
        System.setProperty("INVENTORY_DB_PASSWORD", POSTGRESQL_CONTAINER.getPassword());
    }

    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    TestInventorySavedListener inventorySavedListener;

    @Test
    void shouldCreateAndReturnInventory() {
        var id = UUID.randomUUID();

        Assertions.assertThat(getInventory(id, ErrorResponse.class))
                .matches(r -> r.getStatusCode().equals(HttpStatus.NOT_FOUND)
                        && r.getBody().equals(ErrorResponse.fromException(new InventoryNotFoundException(id))));

        Assertions.assertThat(inventorySavedListener.capturedEvent()).isNull();

        var inventory = new Inventory(id, List.of("001", "002"),
                Instant.now().truncatedTo(ChronoUnit.MILLIS));

        Assertions.assertThat(saveInventory(inventory))
                .matches(r -> r.getStatusCode().is2xxSuccessful());

        Assertions.assertThat(getInventory(id))
                .matches(r -> r.getStatusCode().is2xxSuccessful()
                        && r.getBody().equals(inventory));

        Assertions.assertThat(inventorySavedListener.capturedEvent())
                .isEqualTo(new InventorySavedEvent(inventory.id()));
    }

    private <T> ResponseEntity<T> getInventory(UUID id, Class<T> response) {
        return restTemplate.getForEntity("/inventories/" + id, response);
    }

    private ResponseEntity<Inventory> getInventory(UUID id) {
        return getInventory(id, Inventory.class);
    }

    private ResponseEntity<Void> saveInventory(Inventory inventory) {
        return restTemplate.exchange(RequestEntity.put("/inventories").body(inventory), Void.class);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        TestInventorySavedListener testInventorySavedListener() {
            return new TestInventorySavedListener();
        }

    }

    static class TestInventorySavedListener {

        private final AtomicReference<InventorySavedEvent> capturedEvent = new AtomicReference<>();

        @EventListener
        void onInventorySaved(InventorySavedEvent event) {
            capturedEvent.set(event);
        }

        public InventorySavedEvent capturedEvent() {
            return capturedEvent.get();
        }
    }
}
