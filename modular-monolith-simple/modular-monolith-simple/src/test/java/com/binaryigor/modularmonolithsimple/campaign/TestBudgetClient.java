package com.binaryigor.modularmonolithsimple.campaign;

import com.binaryigor.modularmonolithsimple._contracts.BudgetClient;

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
