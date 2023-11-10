package com.binaryigor.modularmonolith.campaign;

import com.binaryigor.modularmonolith.contracts.InventoryClient;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TestInventoryClient implements InventoryClient {

    private final Set<UUID> inventories = new HashSet<>();

    public void addInventory(UUID id) {
        inventories.add(id);
    }

    @Override
    public boolean inventoryExists(UUID id) {
        return inventories.contains(id);
    }
}
