package com.binaryigor.modularmonolith.budget;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    private final Clock clock;

    public BudgetController(Clock clock) {
        this.clock = clock;
    }

    @PutMapping
    void save(Budget budget) {
        System.out.println("Saving budget:%s on %s instant".formatted(budget, clock.instant()));
    }

    @GetMapping("{id}")
    Budget get(@PathVariable(name = "id") UUID id) {
        return new Budget(id, BigDecimal.TEN, 1);
    }
}
