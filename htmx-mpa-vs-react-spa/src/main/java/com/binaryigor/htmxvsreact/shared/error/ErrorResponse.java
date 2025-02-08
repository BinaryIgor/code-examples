package com.binaryigor.htmxvsreact.shared.error;

public record ErrorResponse(String exception, String message) {

    public static ErrorResponse fromException(Throwable exception) {
        return new ErrorResponse(exception.getClass().getSimpleName(), exception.getMessage());
    }
}
