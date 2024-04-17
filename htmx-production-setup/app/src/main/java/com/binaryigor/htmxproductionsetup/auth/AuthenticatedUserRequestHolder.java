package com.binaryigor.htmxproductionsetup.auth;

import com.binaryigor.htmxproductionsetup.shared.contracts.UserData;
import com.binaryigor.htmxproductionsetup.shared.web.HttpRequestAttributes;

import java.util.Optional;
import java.util.UUID;

public class AuthenticatedUserRequestHolder {

    public static void set(UserData user) {
        HttpRequestAttributes.set(HttpRequestAttributes.USER_ATTRIBUTE, user);
    }

    public static Optional<UserData> get() {
        return HttpRequestAttributes.get(HttpRequestAttributes.USER_ATTRIBUTE, UserData.class);
    }
}
