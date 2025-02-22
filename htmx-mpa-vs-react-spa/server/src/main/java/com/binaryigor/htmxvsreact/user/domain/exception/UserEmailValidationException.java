package com.binaryigor.htmxvsreact.user.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.ValidationException;

public class UserEmailValidationException extends ValidationException {

    public UserEmailValidationException() {
        super("Given email is not valid");
    }
}
