package com.binaryigor.htmxproductionsetup.shared.exception;

public class NotFoundException extends AppException {

    public final String resource;

    public NotFoundException(String resource) {
        this.resource = resource;
    }
}
