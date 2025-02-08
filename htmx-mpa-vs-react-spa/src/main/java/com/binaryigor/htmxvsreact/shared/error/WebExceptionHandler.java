package com.binaryigor.htmxvsreact.shared.error;

import com.binaryigor.htmxvsreact.shared.HTMX;
import com.binaryigor.htmxvsreact.shared.WebUtils;
import com.binaryigor.htmxvsreact.shared.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.shared.html.Translations;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WebExceptionHandler {

    private final HTMLTemplates htmlTemplates;
    private final Translations translations;

    public WebExceptionHandler(HTMLTemplates htmlTemplates, Translations translations) {
        this.htmlTemplates = htmlTemplates;
        this.translations = translations;
    }

    public ResponseEntity<?> handle(HttpStatus status, Throwable throwable) {
        if (!shouldRespondWithHTML()) {
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

    private boolean shouldRespondWithHTML() {
        return WebUtils.currentRequest()
            .map(r -> {
                var contentType = r.getHeader("content-type");
                if (contentType != null && contentType.contains("json")) {
                    return false;
                }
                var accept = r.getHeader("accept");
                return accept == null || !accept.contains("json");
            })
            .orElse(true);
    }

    private String errorPage(String error) {
        return htmlTemplates.renderPage("error-page.mustache",
            Map.of("title", translations.message("error-page.title"),
                "error", error));
    }

    // TODO: JSON version?
    public void handle(HttpServletResponse response, HttpStatus status, Throwable throwable) {
        var translatedError = translations.error(throwable);
        String responseContentType;
        String responseBody;
        if (HTMX.isHTMXRequest()) {
            responseContentType = MediaType.TEXT_PLAIN_VALUE;
            responseBody = translatedError;
        } else {
            responseContentType = MediaType.TEXT_HTML_VALUE;
            responseBody = errorPage(translatedError);
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
}
