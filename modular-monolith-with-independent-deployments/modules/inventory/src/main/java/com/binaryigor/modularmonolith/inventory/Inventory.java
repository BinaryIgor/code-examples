package com.binaryigor.modularmonolith.inventory;

import java.util.List;
import java.util.UUID;

public record Inventory(UUID id, List<String> skus, long version) {
}
