package com.binaryigor.htmxproductionsetup.shared.exception;

public class AccessForbiddenException extends AppException {

    public AccessForbiddenException(String message) {
        super(message);
    }

    public AccessForbiddenException() {
        this("Current user doesn't have access to requested resource");
    }
}
