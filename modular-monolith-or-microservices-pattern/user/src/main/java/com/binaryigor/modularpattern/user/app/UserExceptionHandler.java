package com.binaryigor.modularpattern.user.app;

import com.binaryigor.modularpattern.user.domain.exception.OptimisticLockException;
import com.binaryigor.modularpattern.user.domain.exception.UserDoesNotExistException;
import com.binaryigor.modularpattern.user.domain.exception.UserEmailTakenException;
import com.binaryigor.modularpattern.user.domain.exception.UserIdTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
public class UserExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    ProblemDetail handleUserIdTakenException(UserIdTakenException e) {
        return fromException(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler
    ProblemDetail handleUserEmailTakenException(UserEmailTakenException e) {
        return fromException(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler
    ProblemDetail handleUserDoesNotExistException(UserDoesNotExistException e) {
        return fromException(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler
    ProblemDetail handleOptimisticLockException(OptimisticLockException e) {
        return fromException(HttpStatus.CONFLICT, e);
    }

    private ProblemDetail fromException(HttpStatus status, Throwable throwable) {
        var detail = ProblemDetail.forStatus(status);
        detail.setType(URI.create(throwable.getClass().getSimpleName()));
        detail.setTitle(throwable.getClass().getSimpleName());
        detail.setDetail(throwable.getMessage());
        return detail;
    }
}
