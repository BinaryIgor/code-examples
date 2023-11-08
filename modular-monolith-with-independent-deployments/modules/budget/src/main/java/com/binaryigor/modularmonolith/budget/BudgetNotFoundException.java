package com.binaryigor.modularmonolith.budget;

import java.util.UUID;

public class BudgetNotFoundException extends RuntimeException {

    public BudgetNotFoundException(UUID id) {
        super("Budget of %s id doesn't exist".formatted(id));
    }
}
