package com.binaryigor.modularpattern.user.domain;

import java.util.UUID;

public class UserDoesNotExistException extends RuntimeException {

    public UserDoesNotExistException(String message) {
        super(message);
    }

    public static UserDoesNotExistException ofId(UUID id) {
        return new UserDoesNotExistException("User of %s id does not exist".formatted(id));
    }

    public static UserDoesNotExistException ofEmail(String email) {
        return new UserDoesNotExistException("User of %s email does not exist".formatted(email));
    }
}
