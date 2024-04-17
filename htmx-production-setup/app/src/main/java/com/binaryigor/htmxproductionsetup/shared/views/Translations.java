package com.binaryigor.htmxproductionsetup.shared.views;

import com.binaryigor.htmxproductionsetup.shared.Language;
import com.binaryigor.htmxproductionsetup.shared.exception.AppException;
import com.binaryigor.htmxproductionsetup.shared.exception.NotFoundException;
import com.binaryigor.htmxproductionsetup.shared.web.HttpRequestAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Translations {

    private static final Logger logger = LoggerFactory.getLogger(Translations.class);
    private static final Map<String, ExceptionTranslator> EXCEPTIONS_TRANSLATIONS = new HashMap<>();
    private static final String UNKNOWN_EXCEPTION_TRANSLATION = "Unknown error has occurred";
    private static Language language;

    static {
        registerExceptionTranslator(AppException.class, (t, l) -> appException(l));
        registerExceptionTranslator(NotFoundException.class, (t, l) -> notFoundException(l, t.resource));
    }

    public static String appException(Language language) {
        return switch (language) {
            case EN -> "Bad request, should not happen!";
        };
    }

    public static String notFoundException(Language language, String resource) {
        return switch (language) {
            case EN -> "%s was not found".formatted(resource);
        };
    }

    public static Language currentLanguage() {
        if (language == null) {
            return HttpRequestAttributes.get(HttpRequestAttributes.REQUEST_LANGUAGE_ATTRIBUTE, Language.class)
                    .orElseThrow(() -> new IllegalStateException("Request must have language set"));
        }
        return language;
    }

    // Only for tests
    public static void setCurrentLanguage(Language language) {
        Translations.language = language;
    }

    public static <T extends Throwable> void registerExceptionTranslator(
            Class<T> exception,
            ExceptionTranslator<T> translator) {
        EXCEPTIONS_TRANSLATIONS.put(exceptionKey(exception), translator);
    }

    private static String exceptionKey(Class<? extends Throwable> exception) {
        return exception.getSimpleName();
    }

    public static String signIn() {
        return switch (currentLanguage()) {
            case EN -> "Sign In";
        };
    }

    public static String signOut() {
        return switch (currentLanguage()) {
            case EN -> "Sign Out";
        };
    }

    public static String homeStart() {
        return switch (currentLanguage()) {
            case EN -> "Today";
        };
    }

    public static String homeHistory() {
        return switch (currentLanguage()) {
            case EN -> "History";
        };
    }

    public static String hello(String name) {
        return switch (currentLanguage()) {
            case EN -> "Hello %s!".formatted(name);
        };
    }

    public static String dayStart(String user, LocalDate date) {
        return switch (currentLanguage()) {
            case EN -> "Hello %s! Let's start the day %s".formatted(user, date);
        };
    }

    public static String history(LocalDate date) {
        return switch (currentLanguage()) {
            case EN -> "History as of %s date".formatted(date);
        };
    }

    public static String historyOfDay(LocalDate date) {
        return switch (currentLanguage()) {
            case EN -> "Some history of the day %s".formatted(date);
        };
    }

    public static String dayDescription() {
        return switch (currentLanguage()) {
            case EN -> "Description";
        };
    }

    public static String notFoundTitle() {
        return switch (currentLanguage()) {
            case EN -> "Not Found";
        };
    }

    public static String notFoundMessage(String notFoundPath) {
        return switch (currentLanguage()) {
            case EN ->
                    "Unfortunately, the <span class=\"underline\">%s</span> page was not found.".formatted(notFoundPath);
        };
    }

    public static String backToTheMainPage() {
        return switch (currentLanguage()) {
            case EN -> "Let's get <a class=\"underline\" href=\"/\">back</a> to the main page</a>!";
        };
    }

    public static String unknownErrorTitle() {
        return switch (currentLanguage()) {
            case EN -> "Unknown Error";
        };
    }

    public static String exception(Runnable runnable) {
        try {
            runnable.run();
            return null;
        } catch (Exception e) {
            return exception(e);
        }
    }

    public static String exception(Throwable exception) {
        try {
            return Optional.ofNullable(EXCEPTIONS_TRANSLATIONS.get(exceptionKey(exception.getClass())))
                    .map(t -> t.translate(exception, currentLanguage()))
                    .orElse(UNKNOWN_EXCEPTION_TRANSLATION);
        } catch (Exception e) {
            logger.error("Problem while translating {} exception...", exception.getClass(), e);
            return UNKNOWN_EXCEPTION_TRANSLATION;
        }
    }

    public interface ExceptionTranslator<T extends Throwable> {
        String translate(T exception, Language language);
    }
}
