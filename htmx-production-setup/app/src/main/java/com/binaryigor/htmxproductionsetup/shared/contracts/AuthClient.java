package com.binaryigor.htmxproductionsetup.shared.contracts;

import com.binaryigor.htmxproductionsetup.shared.AuthToken;

import java.util.UUID;

public interface AuthClient {
    AuthToken ofUser(UUID id);
}
