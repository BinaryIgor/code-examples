package com.binaryigor.htmxproductionsetup.shared.contracts;

import java.time.Instant;

public record AuthToken(String value, Instant expiresAt) {
}
