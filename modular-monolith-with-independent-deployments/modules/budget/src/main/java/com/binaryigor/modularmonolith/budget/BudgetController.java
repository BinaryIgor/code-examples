package com.binaryigor.modularmonolith.budget;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    @PutMapping
    void save(Budget budget) {
        System.out.println("Saving budget..." + budget);
    }

    @GetMapping("{id}")
    Budget get(@PathVariable(name = "id") UUID id) {
        return new Budget(id, BigDecimal.TEN, 1);
    }
}
