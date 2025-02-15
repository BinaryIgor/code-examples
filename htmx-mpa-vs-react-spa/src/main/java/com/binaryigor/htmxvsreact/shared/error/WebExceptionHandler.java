package com.binaryigor.htmxvsreact.shared.error;

import com.binaryigor.htmxvsreact.shared.HTMX;
import com.binaryigor.htmxvsreact.shared.WebUtils;
import com.binaryigor.htmxvsreact.shared.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.shared.html.Translations;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WebExceptionHandler {

    private final HTMLTemplates htmlTemplates;
    private final Translations translations;
    private final ObjectMapper objectMapper;

    public WebExceptionHandler(HTMLTemplates htmlTemplates, Translations translations,
                               ObjectMapper objectMapper) {
        this.htmlTemplates = htmlTemplates;
        this.translations = translations;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<?> handle(HttpStatus status, Throwable throwable) {
        if (!WebUtils.shouldRespondWithHTML()) {
            return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.fromException(throwable));
        }

        var translatedError = translations.error(throwable);
        if (HTMX.isHTMXRequest()) {
            return ResponseEntity.status(status)
                .body(translatedError);
        }

        return ResponseEntity.status(status)
            .contentType(MediaType.TEXT_HTML)
            .body(errorPage(translatedError));
    }

    private String errorPage(String error) {
        return htmlTemplates.renderPage("error-page.mustache",
            Map.of("title", translations.message("error-page.title"),
                "error", error));
    }

    // TODO: JSON version?
    public void handle(HttpServletResponse response, HttpStatus status, Throwable throwable) {
        String responseContentType;
        String responseBody;

        if (WebUtils.shouldRespondWithHTML()) {
            var translatedError = translations.error(throwable);
            if (HTMX.isHTMXRequest()) {
                responseContentType = MediaType.TEXT_PLAIN_VALUE;
                responseBody = translatedError;
            } else {
                responseContentType = MediaType.TEXT_HTML_VALUE;
                responseBody = errorPage(translatedError);
            }
        } else {
            responseContentType = MediaType.APPLICATION_JSON_VALUE;
            responseBody = errorAsJson(throwable);
        }

        try {
            response.setStatus(status.value());
            response.setContentType(responseContentType);
            response.setContentLength(responseBody.getBytes(StandardCharsets.UTF_8).length);
            response.getWriter().write(responseBody);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String errorAsJson(Throwable throwable) {
        try {
            return objectMapper.writeValueAsString(ErrorResponse.fromException(throwable));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
