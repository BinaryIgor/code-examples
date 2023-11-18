package com.binaryigor.modularmonolithsimple.budget;

import com.binaryigor.modularmonolithsimple._contracts.BudgetSavedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetRepository budgetRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BudgetController(BudgetRepository budgetRepository,
                            ApplicationEventPublisher eventPublisher) {
        this.budgetRepository = budgetRepository;
        this.eventPublisher = eventPublisher;
    }

    @PutMapping
    void save(@RequestBody Budget budget) {
        budgetRepository.save(budget);
        eventPublisher.publishEvent(new BudgetSavedEvent(budget.id()));
    }

    @GetMapping("{id}")
    Budget get(@PathVariable(name = "id") UUID id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException(id));
    }
}
