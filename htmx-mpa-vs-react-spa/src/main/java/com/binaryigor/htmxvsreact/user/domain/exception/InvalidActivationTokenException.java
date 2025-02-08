package com.binaryigor.htmxvsreact.user.domain.exception;

public class InvalidActivationTokenException extends RuntimeException {

    public InvalidActivationTokenException(String message) {
        super(message);
    }

    public static InvalidActivationTokenException invalid() {
        return new InvalidActivationTokenException("Token is invalid");
    }

    public static InvalidActivationTokenException notFound() {
        return new InvalidActivationTokenException("Token does not exist");
    }

    public static InvalidActivationTokenException expired() {
        return new InvalidActivationTokenException("Token has expired");
    }
}
