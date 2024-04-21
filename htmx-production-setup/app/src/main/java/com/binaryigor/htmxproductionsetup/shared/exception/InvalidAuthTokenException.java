package com.binaryigor.htmxproductionsetup.shared.exception;

public class InvalidAuthTokenException extends AppException {

    public InvalidAuthTokenException(String message) {
        super(message);
    }

    public static InvalidAuthTokenException invalidToken() {
        return new InvalidAuthTokenException("Invalid token");
    }

    public static InvalidAuthTokenException expiredToken() {
        return new InvalidAuthTokenException("Token has expired");
    }
}
