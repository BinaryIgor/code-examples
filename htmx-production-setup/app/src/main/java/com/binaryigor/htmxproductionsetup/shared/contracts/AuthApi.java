package com.binaryigor.htmxproductionsetup.shared.contracts;

import java.util.UUID;

public interface AuthApi {
    AuthToken ofUser(UUID id);
}
