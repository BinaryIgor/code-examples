package com.binaryigor.modularmonolithsimple.campaign;

import com.binaryigor.modularmonolithsimple._contracts.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CampaignExceptionsHandler {

    @ExceptionHandler
    ResponseEntity<ErrorResponse> handleCampaignNotFoundException(CampaignNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.fromException(exception));
    }

    @ExceptionHandler
    ResponseEntity<ErrorResponse> handleCampaignValidationException(CampaignValidationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.fromException(exception));
    }
}
