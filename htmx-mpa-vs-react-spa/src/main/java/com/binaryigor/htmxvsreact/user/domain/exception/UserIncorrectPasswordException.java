package com.binaryigor.htmxvsreact.user.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.ValidationException;

public class UserIncorrectPasswordException extends ValidationException {

    public UserIncorrectPasswordException() {
        super("Given password is not valid");
    }
}
