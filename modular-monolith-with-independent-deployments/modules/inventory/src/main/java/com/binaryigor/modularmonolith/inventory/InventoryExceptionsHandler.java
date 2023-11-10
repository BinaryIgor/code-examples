package com.binaryigor.modularmonolith.inventory;

import com.binaryigor.modularmonolith.contracts.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InventoryExceptionsHandler {

    @ExceptionHandler
    ResponseEntity<ErrorResponse> handleInventoryNotFoundException(InventoryNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.fromException(exception));
    }
}
