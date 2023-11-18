package com.binaryigor.modularmonolithsimple._contracts;

import java.util.UUID;

public interface InventoryClient {

    boolean inventoryExists(UUID id);
}
