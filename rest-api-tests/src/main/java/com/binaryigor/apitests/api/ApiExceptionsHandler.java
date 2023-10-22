package com.binaryigor.apitests.api;

import com.binaryigor.apitests.domain.ClientNotFoundException;
import com.binaryigor.apitests.domain.ClientValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionsHandler {

    @ExceptionHandler
    ResponseEntity<ErrorResponse> handleClientNotFoundException(ClientNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.fromException(exception));
    }

    @ExceptionHandler
    ResponseEntity<ErrorResponse> handleClientValidationException(ClientValidationException exception) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.fromException(exception));
    }
}
