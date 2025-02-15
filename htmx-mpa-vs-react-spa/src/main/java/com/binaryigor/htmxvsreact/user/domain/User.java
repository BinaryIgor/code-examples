package com.binaryigor.htmxvsreact.user.domain;

import com.binaryigor.htmxvsreact.shared.AppLanguage;
import com.binaryigor.htmxvsreact.user.domain.exception.UserEmailValidationException;

import java.util.UUID;

public record User(UUID id, String email, String name, String password, AppLanguage language) {

    public User {
        if (!UserValidator.isEmailValid(email)) {
            throw new UserEmailValidationException();
        }
    }
}
