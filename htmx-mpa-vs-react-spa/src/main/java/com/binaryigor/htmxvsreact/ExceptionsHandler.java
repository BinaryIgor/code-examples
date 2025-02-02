package com.binaryigor.htmxvsreact;

import com.binaryigor.htmxvsreact.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.project.exception.ProjectDoestNotExistException;
import com.binaryigor.htmxvsreact.shared.Translations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ExceptionsHandler {

    private final HTMLTemplates htmlTemplates;

    public ExceptionsHandler(HTMLTemplates htmlTemplates) {
        this.htmlTemplates = htmlTemplates;
    }

    @ExceptionHandler
    ResponseEntity<String> handle(ProjectDoestNotExistException exception) {
        return returnHtmlErrorPage(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler
    ResponseEntity<String> handle(Throwable throwable) {
        return returnHtmlErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, throwable);
    }

    private ResponseEntity<String> returnHtmlErrorPage(HttpStatus status, Throwable throwable) {
        return ResponseEntity.status(status)
            .body(htmlTemplates.renderPage("error-page.mustache",
                Map.of("title", Translations.message("error-page.title"),
                    "error", throwable.getMessage())));
    }
}
