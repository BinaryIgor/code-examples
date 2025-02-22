package com.binaryigor.htmxvsreact.user.infra.web;


import com.binaryigor.htmxvsreact.shared.AppLanguage;
import com.binaryigor.htmxvsreact.shared.HttpRequestAttributes;
import com.binaryigor.htmxvsreact.user.domain.AuthenticatedUser;
import com.binaryigor.htmxvsreact.user.domain.exception.UnauthenticatedException;

import java.util.Optional;
import java.util.UUID;

import static com.binaryigor.htmxvsreact.shared.HttpRequestAttributes.USER_ATTRIBUTE;

public class CurrentRequestUser {

    public static void set(AuthenticatedUser user) {
        HttpRequestAttributes.set(USER_ATTRIBUTE, user);
    }

    public static Optional<AuthenticatedUser> userOpt() {
        return HttpRequestAttributes.get(USER_ATTRIBUTE, AuthenticatedUser.class);
    }

    public static AuthenticatedUser user() {
        return userOpt().orElseThrow(UnauthenticatedException::new);
    }

    public static Optional<UUID> idOpt() {
        return userOpt().map(AuthenticatedUser::id);
    }

    public static UUID id() {
        return user().id();
    }

    public static AppLanguage language() {
        return userOpt().map(AuthenticatedUser::language).orElse(AppLanguage.EN);
    }
}
