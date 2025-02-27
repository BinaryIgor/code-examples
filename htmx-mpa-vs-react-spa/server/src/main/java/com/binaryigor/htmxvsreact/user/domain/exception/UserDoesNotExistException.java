package com.binaryigor.htmxvsreact.user.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.DoesNotExistException;

import java.util.UUID;

public class UserDoesNotExistException extends DoesNotExistException {

    public UserDoesNotExistException(String message) {
        super(message);
    }

    public static UserDoesNotExistException ofId(UUID id) {
        return new UserDoesNotExistException("User of %s id doesn't exist".formatted(id));
    }

    public static UserDoesNotExistException ofEmail(String email) {
        return new UserDoesNotExistException("User of %s email doesn't exist".formatted(email));
    }
}
