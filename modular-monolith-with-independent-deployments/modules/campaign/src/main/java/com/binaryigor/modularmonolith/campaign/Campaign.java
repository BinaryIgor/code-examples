package com.binaryigor.modularmonolith.campaign;

import java.time.LocalDate;
import java.util.UUID;

public record Campaign(UUID id,
                       String name,
                       UUID budgetId,
                       UUID inventoryId,
                       LocalDate startDate,
                       LocalDate endDate) {
}
