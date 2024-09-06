package com.binaryigor.vembeddingswithpostgres;

import com.binaryigor.vembeddingswithpostgres.generator.VectorEmbeddingsGenerator;
import com.binaryigor.vembeddingswithpostgres.shared.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
public class HttpExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    ProblemDetail handle(VectorEmbeddingsGenerator.Exception exception) {
        return exceptionToProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler
    ProblemDetail handle(ResourceNotFoundException exception) {
        return exceptionToProblemDetail(HttpStatus.NOT_FOUND, exception);
    }

    public static ProblemDetail exceptionToProblemDetail(HttpStatus status, Exception exception) {
        var problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setType(URI.create(exception.getClass().getName()));
        problemDetail.setTitle(exception.getClass().getName());
        return problemDetail;
    }
}
