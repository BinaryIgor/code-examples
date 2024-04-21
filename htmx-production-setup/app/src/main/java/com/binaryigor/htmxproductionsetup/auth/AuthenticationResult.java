package com.binaryigor.htmxproductionsetup.auth;

import com.binaryigor.htmxproductionsetup.shared.contracts.UserData;

import java.time.Instant;

public record AuthenticationResult(UserData user,
                                   Instant expiresAt) {
}
