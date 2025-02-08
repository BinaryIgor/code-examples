package com.binaryigor.htmxvsreact.user.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.ValidationException;

public class UserEmailException extends ValidationException {

    public UserEmailException() {
        super("Given email is not valid");
    }
}
