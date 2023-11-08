package com.binaryigor.modularmonolith.inventory;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/inventories")
public class InventoryController {

    private final InventoryRepository repository;

    public InventoryController(InventoryRepository repository) {
        this.repository = repository;
    }

    @PutMapping
    void save(Inventory inventory) {
        repository.save(inventory);
    }

    @GetMapping("{id}")
    Inventory get(@PathVariable(name = "id") UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(id));
    }

}
