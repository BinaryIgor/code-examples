package com.binaryigor.modularmonolith.budget;

import com.binaryigor.modularmonolith.contracts.BudgetClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TheBudgetClient implements BudgetClient {

    private final BudgetRepository budgetRepository;

    public TheBudgetClient(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public boolean budgetExists(UUID id) {
        return budgetRepository.findById(id).isPresent();
    }
}
