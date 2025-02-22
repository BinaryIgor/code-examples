package com.binaryigor.htmxvsreact.user.domain.exception;

public class AccessForbiddenException extends RuntimeException {

    public AccessForbiddenException(String message) {
        super(message);
    }

    public AccessForbiddenException() {
        this("Current user does not have access to the requested resource");
    }
}
