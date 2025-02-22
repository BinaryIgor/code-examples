package com.binaryigor.htmxvsreact.user.domain;

import java.time.Instant;

public record AuthenticationResult(AuthenticatedUser user,
                                   Instant expiresAt) {
}
