package com.binaryigor.htmxvsreact.user.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.ValidationException;

public class UserPasswordValidationException extends ValidationException {

    public UserPasswordValidationException() {
        super("Given password is not valid");
    }
}
