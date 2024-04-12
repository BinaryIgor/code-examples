package com.binaryigor.htmxproductionsetup.auth;

import com.binaryigor.htmxproductionsetup.shared.AuthenticatedUser;
import com.binaryigor.htmxproductionsetup.shared.web.HttpRequestAttributes;

import java.util.Optional;

public class AuthenticatedUserRequestHolder {

    //TODO: set user locale!
    public static void set(AuthenticatedUser user) {
        HttpRequestAttributes.set(HttpRequestAttributes.USER_ATTRIBUTE, user);
        HttpRequestAttributes.set(HttpRequestAttributes.USER_ID_ATTRIBUTE, user.id().toString());
    }

    public static Optional<AuthenticatedUser> get() {
        return HttpRequestAttributes.get(HttpRequestAttributes.USER_ATTRIBUTE, AuthenticatedUser.class);
    }
}
