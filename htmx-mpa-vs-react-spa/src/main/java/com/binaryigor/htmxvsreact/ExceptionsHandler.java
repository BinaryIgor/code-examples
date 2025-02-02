package com.binaryigor.htmxvsreact;

import com.binaryigor.htmxvsreact.shared.DoesNotExistException;
import com.binaryigor.htmxvsreact.shared.HTMX;
import com.binaryigor.htmxvsreact.shared.Translations;
import com.binaryigor.htmxvsreact.shared.ValidationException;
import com.binaryigor.htmxvsreact.shared.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.task.exception.TaskProjectDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

@RestControllerAdvice
public class ExceptionsHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionsHandler.class);
    private final HTMLTemplates htmlTemplates;

    public ExceptionsHandler(HTMLTemplates htmlTemplates) {
        this.htmlTemplates = htmlTemplates;
    }

    @ExceptionHandler
    ResponseEntity<String> handle(ValidationException exception) {
        return returnHtmlErrorPageOrTranslation(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler
    ResponseEntity<String> handle(DoesNotExistException exception) {
        return returnHtmlErrorPageOrTranslation(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler
    ResponseEntity<String> handle(TaskProjectDoesNotExistException exception) {
        return returnHtmlErrorPageOrTranslation(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler
    ResponseEntity<String> handle(NoResourceFoundException exception) {
        return returnHtmlErrorPageOrTranslation(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler
    ResponseEntity<String> handle(MissingRequestValueException exception) {
        return returnHtmlErrorPageOrTranslation(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler
    ResponseEntity<String> handle(Throwable throwable) {
        logger.error("Unhandled exception, should never happen!", throwable);
        return returnHtmlErrorPageOrTranslation(HttpStatus.INTERNAL_SERVER_ERROR, throwable);
    }

    private ResponseEntity<String> returnHtmlErrorPageOrTranslation(HttpStatus status, Throwable throwable) {
        var translatedError = Translations.error(throwable);
        if (HTMX.isHTMXRequest()) {
            return ResponseEntity.status(status)
                .body(translatedError);
        }
        return ResponseEntity.status(status)
            .contentType(MediaType.TEXT_HTML)
            .body(htmlTemplates.renderPage("error-page.mustache",
                Map.of("title", Translations.message("error-page.title"),
                    "error", translatedError)));
    }
}
