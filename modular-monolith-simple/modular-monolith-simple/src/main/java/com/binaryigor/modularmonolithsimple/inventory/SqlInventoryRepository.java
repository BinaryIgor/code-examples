package com.binaryigor.modularmonolithsimple.inventory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SqlInventoryRepository implements InventoryRepository {

    private static final String INVENTORY_TABLE = "inventory.inventory";
    private static final String INVENTORY_SKU_TABLE = "inventory.inventory_sku";
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public SqlInventoryRepository(JdbcTemplate jdbcTemplate,
                                  TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void save(Inventory inventory) {
        transactionTemplate.execute(s -> {
            jdbcTemplate.update("""
                            INSERT INTO %s (id, created_at)
                            VALUES (?, ?)
                            ON CONFLICT (id) DO NOTHING;""".formatted(INVENTORY_TABLE),
                    inventory.id(), Date.from(inventory.createdAt()));

            jdbcTemplate.update("DELETE FROM %s WHERE inventory_id = ?".formatted(INVENTORY_SKU_TABLE), inventory.id());

            var inventorySkusArgs = inventory.skus().stream()
                    .map(sku -> new Object[]{inventory.id(), sku})
                    .toList();

            jdbcTemplate.batchUpdate("INSERT INTO %s VALUES (?, ?)".formatted(INVENTORY_SKU_TABLE), inventorySkusArgs);

            return null;
        });
    }

    @Override
    public Optional<Inventory> findById(UUID id) {
        var result = jdbcTemplate.query("SELECT * FROM %s WHERE id = ?".formatted(INVENTORY_TABLE),
                (r, n) -> new Inventory(r.getObject("id", UUID.class), List.of(),
                        r.getTimestamp("created_at").toInstant()),
                id);

        if (result.isEmpty()) {
            return Optional.empty();
        }

        var inventorySkus = jdbcTemplate.query("SELECT sku FROM %s WHERE inventory_id = ?"
                        .formatted(INVENTORY_SKU_TABLE),
                (r, n) -> r.getString(1),
                id);

        var inventoryWithoutSkus = result.getFirst();

        return Optional.of(new Inventory(inventoryWithoutSkus.id(), inventorySkus,
                inventoryWithoutSkus.createdAt()));
    }
}
