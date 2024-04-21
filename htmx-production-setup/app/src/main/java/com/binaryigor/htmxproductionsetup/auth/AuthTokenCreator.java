package com.binaryigor.htmxproductionsetup.auth;

import com.binaryigor.htmxproductionsetup.shared.contracts.AuthToken;

import java.util.UUID;

public interface AuthTokenCreator {

    AuthToken ofUser(UUID id);
}
