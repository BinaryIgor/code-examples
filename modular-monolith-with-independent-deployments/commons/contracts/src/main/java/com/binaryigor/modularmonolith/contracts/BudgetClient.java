package com.binaryigor.modularmonolith.contracts;

import java.util.UUID;

public interface BudgetClient {

    boolean budgetExists(UUID id);
}
