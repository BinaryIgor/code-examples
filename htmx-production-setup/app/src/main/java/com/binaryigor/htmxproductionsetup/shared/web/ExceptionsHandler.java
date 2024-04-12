package com.binaryigor.htmxproductionsetup.shared.web;

import com.binaryigor.htmxproductionsetup.shared.exception.AppException;
import com.binaryigor.htmxproductionsetup.shared.exception.NotFoundException;
import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler
    ResponseEntity<String> handleNotFoundException(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Translations.exception(exception));
    }

    @ExceptionHandler
    ResponseEntity<String> handleAppException(AppException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Translations.exception(exception));
    }
}
