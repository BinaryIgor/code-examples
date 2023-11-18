package com.binaryigor.modularmonolithsimple.inventory;

import com.binaryigor.modularmonolithsimple._contracts.InventoryClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TheInventoryClient implements InventoryClient {

    private final InventoryRepository inventoryRepository;

    public TheInventoryClient(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public boolean inventoryExists(UUID id) {
        return inventoryRepository.findById(id).isPresent();
    }
}
