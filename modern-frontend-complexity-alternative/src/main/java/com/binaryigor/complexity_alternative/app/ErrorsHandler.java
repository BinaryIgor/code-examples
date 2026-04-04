package com.binaryigor.complexity_alternative.app;

import com.binaryigor.complexity_alternative.domain.DeviceNotFoundException;
import com.binaryigor.complexity_alternative.domain.DevicesException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Controller
@ControllerAdvice
public class ErrorsHandler implements ErrorController {

    private static final Map<String, String> EXCEPTIONS_TO_TRANSLATION_KEYS = Map.of(
            DevicesException.class.getSimpleName(), "errors.devices-exception",
            DeviceNotFoundException.class.getSimpleName(), "errors.device-not-found-exception"
    );
    private static final String DEFAULT_EXCEPTION_TRANSLATION_KEY = "errors.unhandled-exception";
    private final TemplatesResolver templatesResolver;
    private final Translations translations;
    private final ErrorAttributes errorAttributes;

    public ErrorsHandler(TemplatesResolver templatesResolver, Translations translations, ErrorAttributes errorAttributes) {
        this.templatesResolver = templatesResolver;
        this.translations = translations;
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public String returnErrorPage(HttpServletRequest request, Model model, Locale locale) {
        var webRequest = new ServletWebRequest(request);
        var errorTranslationKey = Optional.ofNullable(errorAttributes.getError(webRequest))
                .map(e -> e.getClass().getSimpleName())
                .map(EXCEPTIONS_TO_TRANSLATION_KEYS::get)
                .orElse(DEFAULT_EXCEPTION_TRANSLATION_KEY);


        if (HTMX.isHTMXRequest()) {
            return resolveErrorInfo(model, locale, errorTranslationKey);
        }

        return resolveErrorPage(model, locale, errorTranslationKey);
    }

    @ExceptionHandler
    ResponseEntity<?> handle(DevicesException exception, Locale locale, Model model) {
        return handleTranslatableException(exception, locale, model, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ExceptionHandler
    ResponseEntity<?> handle(DeviceNotFoundException exception, Locale locale, Model model) {
        return handleTranslatableException(exception, locale, model, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<?> handleTranslatableException(Exception exception, Locale locale, Model model, HttpStatus status) {
        var translationKey = EXCEPTIONS_TO_TRANSLATION_KEYS.get(exception.getClass().getSimpleName());
        if (HTMX.isHTMXRequest()) {
            return ResponseEntity.status(status)
                    .body(renderErrorInfo(model, locale, translationKey));
        }
        return ResponseEntity.status(status)
                .body(renderErrorPage(model, locale, translationKey));
    }

    private String resolveErrorInfo(Model model, Locale locale, String errorTranslationKey) {
        translations.enrich(model, locale, Map.of(errorTranslationKey, "error-info.message"),
                "error-info.title", errorTranslationKey);
        return "error-info";
    }
    private String renderErrorInfo(Model model, Locale locale, String errorTranslationKey) {
        translations.enrich(model, locale, Map.of(errorTranslationKey, "error-info.message"),
                "error-info.title", errorTranslationKey);
        return templatesResolver.render("error-info", model, true);
    }

    private String resolveErrorPage(Model model, Locale locale, String errorTranslationKey) {
        translations.enrich(model, locale,
                Map.of("error-page.title", "title",
                        errorTranslationKey, "error-page.message"),
                "error-page.title", errorTranslationKey);
        return templatesResolver.resolve("error-page", model);
    }

    private String renderErrorPage(Model model, Locale locale, String errorTranslationKey) {
        translations.enrich(model, locale,
                Map.of("error-page.title", "title",
                        errorTranslationKey, "error-page.message"),
                "error-page.title", errorTranslationKey);
        return templatesResolver.render("error-page", model, false);
    }
}
