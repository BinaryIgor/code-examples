package com.binaryigor.modularmonolith.inventory;

import com.binaryigor.modularmonolith.contracts.InventorySavedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/inventories")
public class InventoryController {

    private final InventoryRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public InventoryController(InventoryRepository repository,
                               ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @PutMapping
    void save(@RequestBody Inventory inventory) {
        repository.save(inventory);
        eventPublisher.publishEvent(new InventorySavedEvent(inventory.id()));
    }

    @GetMapping("{id}")
    Inventory get(@PathVariable(name = "id") UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(id));
    }

}
