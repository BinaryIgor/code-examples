package com.binaryigor.htmxvsreact.user.domain.exception;

public class InvalidAuthTokenException extends RuntimeException {

    public InvalidAuthTokenException(String message) {
        super(message);
    }

    public static InvalidAuthTokenException invalid() {
        return new InvalidAuthTokenException("Token is invalid");
    }

    public static InvalidAuthTokenException expired() {
        return new InvalidAuthTokenException("Token has expired");
    }
}
