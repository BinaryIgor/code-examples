package com.binaryigor.htmxproductionsetup.user.domain.exception;

import com.binaryigor.htmxproductionsetup.shared.exception.AppException;

public class InvalidEmailException extends AppException {

    public final String email;

    public InvalidEmailException(String email) {
        this.email = email;
    }
}
