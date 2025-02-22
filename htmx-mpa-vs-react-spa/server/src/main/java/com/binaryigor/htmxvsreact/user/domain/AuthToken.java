package com.binaryigor.htmxvsreact.user.domain;

import java.time.Instant;

public record AuthToken(String value, Instant expiresAt) {
}
