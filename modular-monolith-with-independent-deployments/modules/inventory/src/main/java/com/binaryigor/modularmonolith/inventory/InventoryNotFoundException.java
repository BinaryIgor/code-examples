package com.binaryigor.modularmonolith.inventory;

import java.util.UUID;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(UUID id) {
        super("Inventory of %s id doesn't exist".formatted(id));
    }
}
