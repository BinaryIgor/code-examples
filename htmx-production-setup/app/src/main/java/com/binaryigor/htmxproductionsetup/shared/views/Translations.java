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
    @SuppressWarnings("rawtypes")
    private static final Map<String, ExceptionTranslation> EXCEPTIONS_TRANSLATIONS = new HashMap<>();
    private static final ThreadLocal<Language> currentLanguage = new ThreadLocal<>();

    static {
        registerExceptionTranslation(AppException.class, (t, l) -> appException(l));
        registerExceptionTranslation(NotFoundException.class, (t, l) -> notFoundException(l, t.resource));
    }

    public static String appException(Language language) {
        return switch (language) {
            case EN -> "Bad request, should not happen!";
        };
    }

    public static String notFoundException(Language language, String resource) {
        return switch (language) {
            case EN -> "%s was not found.".formatted(resource);
        };
    }

    public static String unknownException() {
        return switch (currentLanguage()) {
            case EN -> "Unknown error has occurred";
        };
    }

    public static Language currentLanguage() {
        var language = currentLanguage.get();

        if (language == null) {
            language = HttpRequestAttributes.get(HttpRequestAttributes.REQUEST_LANGUAGE_ATTRIBUTE, Language.class)
                    .orElseThrow(() -> new IllegalStateException("Request must have language set"));

            currentLanguage.set(language);
        }

        return language;
    }

    // Only for tests
    public static void setCurrentLanguage(Language language) {
        currentLanguage.set(language);
    }

    public static <T extends Throwable> void registerExceptionTranslation(
            Class<T> exception,
            ExceptionTranslation<T> translation) {
        EXCEPTIONS_TRANSLATIONS.put(exceptionKey(exception), translation);
    }

    private static String exceptionKey(Class<? extends Throwable> exception) {
        return exception.getSimpleName();
    }

    public static String appTitle() {
        return switch (currentLanguage()) {
            case EN -> "HTMX Production Setup";
        };
    }

    public static String errorModalTitle() {
        return switch (currentLanguage()) {
            case EN -> "Something went wrong...";
        };
    }

    public static String signIn() {
        return switch (currentLanguage()) {
            case EN -> "Sign In";
        };
    }

    public static String emailPlaceholder() {
        return switch (currentLanguage()) {
            case EN -> "Email";
        };
    }

    public static String passwordPlaceholder() {
        return switch (currentLanguage()) {
            case EN -> "Password";
        };
    }

    public static String signOut() {
        return switch (currentLanguage()) {
            case EN -> "Sign Out";
        };
    }

    public static String homeToday() {
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
            case EN -> "Hello <em>%s</em>!".formatted(name);
        };
    }

    public static String dayStart(LocalDate date) {
        return switch (currentLanguage()) {
            case EN -> "Glorious <strong>%s</strong> day".formatted(date);
        };
    }

    public static String dayNotePlaceholder() {
        return switch (currentLanguage()) {
            case EN -> "Note...";
        };
    }

    public static String notSavedDayChanges() {
        return switch (currentLanguage()) {
            case EN -> "There are some not saved changes...";
        };
    }

    public static String savedDayChanges() {
        return switch (currentLanguage()) {
            case EN -> "Changes saved.";
        };
    }

    public static String saveDay() {
        return switch (currentLanguage()) {
            case EN -> "Save";
        };
    }

    public static String history(LocalDate date) {
        return switch (currentLanguage()) {
            case EN -> "History as of <strong>%s<strong> date".formatted(date);
        };
    }

    public static String historyOfDay(LocalDate date) {
        return switch (currentLanguage()) {
            case EN -> "Some history of the day <strong>%s</strong>".formatted(date);
        };
    }

    public static String dayNote() {
        return switch (currentLanguage()) {
            case EN -> "Note";
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

    public static String exceptionIfThrown(Runnable runnable) {
        try {
            runnable.run();
            return null;
        } catch (Exception e) {
            return exception(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static String exception(Throwable exception) {
        try {
            return Optional.ofNullable(EXCEPTIONS_TRANSLATIONS.get(exceptionKey(exception.getClass())))
                    .map(t -> t.translated(exception, currentLanguage()))
                    .orElse(unknownException());
        } catch (Exception e) {
            logger.error("Problem while translating {} exception...", exception.getClass(), e);
            return unknownException();
        }
    }

    public interface ExceptionTranslation<T extends Throwable> {
        String translated(T exception, Language language);
    }
}
