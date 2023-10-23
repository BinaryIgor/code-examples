package com.binaryigor.restapitests.domain;

public class ClientValidationException extends RuntimeException {

    public ClientValidationException(String message) {
        super(message);
    }
}
