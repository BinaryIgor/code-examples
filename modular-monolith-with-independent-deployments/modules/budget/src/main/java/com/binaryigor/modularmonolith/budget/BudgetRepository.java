package com.binaryigor.modularmonolith.budget;

import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository {

    void save(Budget budget);

    Optional<Budget> findById(UUID id);
}
