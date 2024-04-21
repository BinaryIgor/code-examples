package com.binaryigor.htmxproductionsetup.shared.exception;

public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AppException e) {
            return getClass().equals(e.getClass())
                   && getMessage().equals(e.getMessage());
        }

        return false;
    }
}
