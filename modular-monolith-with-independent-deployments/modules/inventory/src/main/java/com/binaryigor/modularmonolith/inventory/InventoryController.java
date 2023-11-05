package com.binaryigor.modularmonolith.inventory;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventories")
public class InventoryController {

    @PutMapping
    void save(Inventory inventory) {
        System.out.println("Saving inventory..." + inventory);
    }

    @GetMapping("{id}")
    Inventory get(@PathVariable UUID id) {
        return new Inventory(id, List.of("01", "02", "03"), 1);
    }

}
