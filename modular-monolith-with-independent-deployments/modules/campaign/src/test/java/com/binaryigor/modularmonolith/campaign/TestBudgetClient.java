package com.binaryigor.modularmonolith.campaign;

import com.binaryigor.modularmonolith.contracts.BudgetClient;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TestBudgetClient implements BudgetClient {

    private final Set<UUID> budgets = new HashSet<>();

    public void addBudget(UUID id) {
        budgets.add(id);
    }

    @Override
    public boolean budgetExists(UUID id) {
        return budgets.contains(id);
    }
}
