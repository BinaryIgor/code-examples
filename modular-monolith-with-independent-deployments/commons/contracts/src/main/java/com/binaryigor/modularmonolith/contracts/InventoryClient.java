package com.binaryigor.modularmonolith.contracts;

import java.util.UUID;

public interface InventoryClient {

    boolean doesInventoryExist(UUID id);
}
