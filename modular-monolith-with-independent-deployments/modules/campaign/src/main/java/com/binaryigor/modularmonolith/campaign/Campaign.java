package com.binaryigor.modularmonolith.campaign;

import java.time.LocalDate;
import java.util.UUID;

public record Campaign(UUID id, String name, LocalDate startDate, LocalDate endDate) {
}
