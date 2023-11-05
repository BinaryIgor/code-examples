package com.binaryigor.modularmonolith.budget;

import java.math.BigDecimal;
import java.util.UUID;

public record Budget(UUID id, BigDecimal amount, long version) {
}
