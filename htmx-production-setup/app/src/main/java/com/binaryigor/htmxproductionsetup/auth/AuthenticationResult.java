package com.binaryigor.htmxproductionsetup.auth;

import com.binaryigor.htmxproductionsetup.shared.AuthenticatedUser;

import java.time.Instant;

public record AuthenticationResult(AuthenticatedUser user,
                                   Instant expiresAt) {
}
