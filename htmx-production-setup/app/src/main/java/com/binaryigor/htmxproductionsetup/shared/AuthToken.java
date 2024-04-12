package com.binaryigor.htmxproductionsetup.shared;

import java.time.Instant;

public record AuthToken(String value, Instant expiresAt) {
}
