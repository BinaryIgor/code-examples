package com.binaryigor.htmxvsreact.user.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.ValidationException;

public class UserPasswordException extends ValidationException {

    public UserPasswordException() {
        super("Given password is not valid");
    }
}
