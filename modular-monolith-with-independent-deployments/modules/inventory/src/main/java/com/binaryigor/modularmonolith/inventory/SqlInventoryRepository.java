package com.binaryigor.modularmonolith.inventory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SqlInventoryRepository implements InventoryRepository {

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
                            INSERT INTO inventory (id, created_at)
                            VALUES (?, ?)
                            ON CONFLICT (id) DO NOTHING;""",
                    inventory.id(), Date.from(inventory.createdAt()));

            jdbcTemplate.update("DELETE FROM inventory_sku WHERE inventory_id = ?", inventory.id());

            var inventorySkusArgs = inventory.skus().stream()
                    .map(sku -> new Object[]{inventory.id(), sku})
                    .toList();

            jdbcTemplate.batchUpdate("INSERT INTO inventory_sku VALUES (?, ?)", inventorySkusArgs);

            return null;
        });
    }

    @Override
    public Optional<Inventory> findById(UUID id) {
        var result = jdbcTemplate.query("SELECT * FROM inventory WHERE id = ?",
                (r, n) -> new Inventory(r.getObject("id", UUID.class), List.of(),
                        r.getTimestamp("created_at").toInstant()),
                id);

        if (result.isEmpty()) {
            return Optional.empty();
        }

        var inventorySkus = jdbcTemplate.query("SELECT sku FROM inventory_sku WHERE inventory_id = ?",
                (r, n) -> r.getString(1),
                id);

        var inventoryWithoutSkus = result.getFirst();

        return Optional.of(new Inventory(inventoryWithoutSkus.id(), inventorySkus,
                inventoryWithoutSkus.createdAt()));
    }
}
