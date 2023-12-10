package com.binaryigor.modularmonolithsimple.inventory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Inventory(UUID id, List<String> skus, Instant createdAt) {
}
