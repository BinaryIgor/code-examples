package com.binaryigor.htmxproductionsetup.shared.exception;

public class UnauthenticatedException extends AppException {

    public UnauthenticatedException() {
        super("Authentication required");
    }
}
