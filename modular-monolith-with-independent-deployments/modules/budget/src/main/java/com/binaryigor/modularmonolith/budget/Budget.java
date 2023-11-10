package com.binaryigor.modularmonolith.budget;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Budget(UUID id, BigDecimal amount, Instant createdAt) {
}
