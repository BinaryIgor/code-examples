package com.binaryigor.htmxproductionsetup.shared.contracts;

import java.util.UUID;

public interface AuthApi {
    AuthToken tokenOfUser(UUID id);
}
