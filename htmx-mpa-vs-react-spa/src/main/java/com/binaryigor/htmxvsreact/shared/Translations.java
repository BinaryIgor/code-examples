package com.binaryigor.htmxvsreact.shared;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO: current user language
public class Translations {

    private static final Map<String, String> TRANSLATIONS = new ConcurrentHashMap<>();
    @SuppressWarnings("rawtypes")
    private static final Map<String, ExceptionTranslator> EXCEPTION_TRANSLATIONS = new ConcurrentHashMap<>();

    static {
        TRANSLATIONS.putAll(Map.of(
            "projects.title", "Projects",
            "projects.delete-modal-title", "Delete project",
            "projects.delete-modal-left", "Cancel",
            "projects.delete-modal-content-template", "Are you sure to delete {project} project?",
            "projects.delete-modal-right", "Ok"));

        TRANSLATIONS.putAll(Map.of("project.create", "Create",
            "project.save", "Save"));

        TRANSLATIONS.putAll(Map.of("tasks.title", "Tasks",
            "tasks.no-tasks", "There are no tasks.",
            "tasks.add", "Add new task"));

        TRANSLATIONS.put("error-page.title", "Something went wrong...");
    }

    public static void register(String key, String message) {
        TRANSLATIONS.put(key, message);
    }

    public static <T extends Throwable> void register(Class<T> key, ExceptionTranslator<T> translator) {
        EXCEPTION_TRANSLATIONS.put(key.getSimpleName(), translator);
    }

    public static String message(String key) {
        return TRANSLATIONS.getOrDefault(key, key);
    }

    @SuppressWarnings("unchecked")
    public static String error(Throwable key) {
        return EXCEPTION_TRANSLATIONS.computeIfAbsent(key.getClass().getSimpleName(), k -> e -> key.getMessage())
            .translate(key);
    }

    @SuppressWarnings("unchecked")
    public static String error(Class<? extends Throwable> key) {
        return EXCEPTION_TRANSLATIONS.computeIfAbsent(key.getSimpleName(), k -> e -> key.getSimpleName()).translate(null);
    }

    public static void copyAll(Map<String, Object> target) {
        target.putAll(TRANSLATIONS);
    }

    public interface ExceptionTranslator<T extends Throwable> {
        String translate(T exception);
    }
}
