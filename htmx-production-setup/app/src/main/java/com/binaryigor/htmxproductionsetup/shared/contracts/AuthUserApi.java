package com.binaryigor.htmxproductionsetup.shared.contracts;

import java.util.UUID;

public interface AuthUserApi {

    UUID currentUserId();

    UserData currentUser();
}
