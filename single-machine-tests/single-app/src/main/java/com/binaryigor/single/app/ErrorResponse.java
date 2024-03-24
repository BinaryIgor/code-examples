package com.binaryigor.single.app;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record ErrorResponse(String code, String message) {

    public static ErrorResponse fromException(Exception exception) {
        return new ErrorResponse(exception.getClass().getSimpleName(), exception.getMessage());
    }

    public static ResponseEntity<ErrorResponse> asResponseEntity(HttpStatus status,
                                                                 Exception exception) {
        return ResponseEntity.status(status)
                .body(fromException(exception));
    }
}
