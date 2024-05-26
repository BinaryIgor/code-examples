package com.binaryigor.modularpattern.project.app;

import com.binaryigor.modularpattern.project.domain.ProjectDoesNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
public class ProjectExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    ProblemDetail handleProjectDoesNotExistException(ProjectDoesNotExistException e) {
        return fromException(HttpStatus.NOT_FOUND, e);
    }

    private ProblemDetail fromException(HttpStatus status, Throwable throwable) {
        var detail = ProblemDetail.forStatus(status);
        detail.setType(URI.create(throwable.getClass().getSimpleName()));
        detail.setTitle(throwable.getClass().getSimpleName());
        detail.setDetail(throwable.getMessage());
        return detail;
    }
}
