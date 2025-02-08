package com.binaryigor.htmxvsreact;

import com.binaryigor.htmxvsreact.shared.error.WebExceptionHandler;
import com.binaryigor.htmxvsreact.shared.exception.ConflictException;
import com.binaryigor.htmxvsreact.shared.exception.DoesNotExistException;
import com.binaryigor.htmxvsreact.shared.exception.OwnerException;
import com.binaryigor.htmxvsreact.shared.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ExceptionsHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionsHandler.class);
    private final WebExceptionHandler webExceptionHandler;

    public ExceptionsHandler(WebExceptionHandler webExceptionHandler) {
        this.webExceptionHandler = webExceptionHandler;
    }

    @ExceptionHandler
    ResponseEntity<?> handleForbidden(OwnerException exception) {
        return webExceptionHandler.handle(HttpStatus.FORBIDDEN, exception);
    }

    @ExceptionHandler({DoesNotExistException.class, NoResourceFoundException.class})
    ResponseEntity<?> handleNotFound(Exception exception) {
        return webExceptionHandler.handle(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler({MissingRequestValueException.class, HttpRequestMethodNotSupportedException.class,
        ValidationException.class})
    ResponseEntity<?> handleBadRequest(Exception exception) {
        return webExceptionHandler.handle(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler
    ResponseEntity<?> handleConflict(ConflictException exception) {
        return webExceptionHandler.handle(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler
    ResponseEntity<?> handle(Throwable throwable) {
        logger.error("Unhandled exception, should never happen!", throwable);
        return webExceptionHandler.handle(HttpStatus.INTERNAL_SERVER_ERROR, throwable);
    }
}
