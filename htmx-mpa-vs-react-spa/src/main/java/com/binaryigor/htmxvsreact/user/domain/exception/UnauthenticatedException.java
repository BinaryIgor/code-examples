package com.binaryigor.htmxvsreact.user.domain.exception;

public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException() {
        super("Authentication required");
    }
}
