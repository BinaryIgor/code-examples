package com.binaryigor.modularmonolith.budget;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetRepository budgetRepository;

    public BudgetController(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @PutMapping
    void save(@RequestBody Budget budget) {
        budgetRepository.save(budget);
    }

    @GetMapping("{id}")
    Budget get(@PathVariable(name = "id") UUID id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException(id));
    }
}
