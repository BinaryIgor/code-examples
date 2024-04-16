package com.binaryigor.htmxproductionsetup.shared.contracts;

import java.util.UUID;

public interface UserApi {
    UserData userOfId(UUID id);
}
