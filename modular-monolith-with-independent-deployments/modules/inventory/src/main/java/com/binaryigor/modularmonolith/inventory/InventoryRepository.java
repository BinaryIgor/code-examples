package com.binaryigor.modularmonolith.inventory;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {

    void save(Inventory inventory);

    Optional<Inventory> findById(UUID id);
}
