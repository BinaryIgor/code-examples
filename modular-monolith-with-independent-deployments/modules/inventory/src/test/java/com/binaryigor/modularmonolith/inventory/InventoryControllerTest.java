package com.binaryigor.modularmonolith.inventory;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

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
    InventoryController controller;

    @Test
    void shouldCreateAndReturnInventory() {
        var id = UUID.randomUUID();

        Assertions.assertThatThrownBy(() -> controller.get(id))
                .isInstanceOf(InventoryNotFoundException.class)
                .hasMessageContaining(id.toString());

        var inventory = new Inventory(id, List.of("001", "002"),
                Instant.now().truncatedTo(ChronoUnit.MILLIS));

        controller.save(inventory);

        Assertions.assertThat(controller.get(id)).isEqualTo(inventory);
    }

}
