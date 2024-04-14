package com.binaryigor.htmxproductionsetup.shared.views;

import com.binaryigor.htmxproductionsetup.shared.exception.AppException;
import com.binaryigor.htmxproductionsetup.shared.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class Translations {

    private static final Logger logger = LoggerFactory.getLogger(Translations.class);
    private static final Map<String, ExceptionTranslator> EXCEPTIONS_TRANSLATIONS = new HashMap<>();
    private static final String UNKNOWN_EXCEPTION_TRANSLATION = "Unknown error has occurred";

    static {
        registerExceptionTranslator(AppException.class, (t, l) -> "Bad request, should not happen!");
        registerExceptionTranslator(NotFoundException.class, (t, l) -> "%s was not found".formatted(t.resource));
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
        return "Sign In";
    }

    public static String signOut() {
        return "Sign Out";
    }

    public static String homeStart() {
        return "Today";
    }

    public static String homeHistory() {
        return "History";
    }

    public static String hello(String name) {
        return "Hello %s!".formatted(name);
    }

    public static String day(Instant date) {
        return "Let's start the day %s".formatted(date);
    }

    public static String history(Instant date) {
        return "History as of %s date".formatted(date);
    }

    public static String notFoundTitle() {
        return "Not Found";
    }

    public static String notFoundMessage(String notFoundPath) {
        return "Unfortunately, the <span class=\"underline\">%s</span> page was not found.".formatted(notFoundPath);
    }

    public static String backToTheMainPage() {
        return "Let's get <a class=\"underline\" href=\"/\">back</a> to the main page</a>!";
    }

    public static String unknownErrorTitle() {
        return "Unknown Error";
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
                    .map(t -> t.translate(exception, Locale.ENGLISH))
                    .orElse(UNKNOWN_EXCEPTION_TRANSLATION);
        } catch (Exception e) {
            logger.error("Problem while translating {} exception...", exception.getClass(), e);
            return UNKNOWN_EXCEPTION_TRANSLATION;
        }
    }

    public interface ExceptionTranslator<T extends Throwable> {
        String translate(T exception, Locale locale);
    }
}
