package com.binaryigor.htmxproductionsetup.user.domain;

import com.binaryigor.htmxproductionsetup.shared.AuthToken;

import java.util.UUID;

public record SignedInUser(UUID id,
                           String email,
                           String name,
                           AuthToken authToken) {
}
