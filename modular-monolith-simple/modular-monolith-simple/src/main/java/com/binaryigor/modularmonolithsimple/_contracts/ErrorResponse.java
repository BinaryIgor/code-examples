package com.binaryigor.modularmonolithsimple._contracts;

public record ErrorResponse(String error, String message) {

    public static ErrorResponse fromException(Throwable exception) {
        return new ErrorResponse(exceptionAsError(exception.getClass()), exception.getMessage());
    }

    public static String exceptionAsError(Class<? extends Throwable> exception) {
        return exception.getSimpleName();
    }
}
