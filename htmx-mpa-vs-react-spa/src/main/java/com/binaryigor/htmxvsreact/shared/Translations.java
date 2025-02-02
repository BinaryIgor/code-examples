package com.binaryigor.htmxvsreact.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// TODO: current user language
public class Translations {

    private static final Map<String, Object> TRANSLATIONS = new ConcurrentHashMap<>();
    @SuppressWarnings("rawtypes")
    private static final Map<String, ExceptionTranslator> EXCEPTION_TRANSLATIONS = new ConcurrentHashMap<>();

    static {
        TRANSLATIONS.put("sign-in",
            Map.of("title", "Sign In",
                "email-placeholder", "Email",
                "password-placeholder", "Password",
                "sign-in", "Sign in"));

        TRANSLATIONS.put("navigation",
            Map.of("projects", "Projects",
                "tasks", "Tasks",
                "account", "Account"));

        TRANSLATIONS.put("projects", Map.of(
            "title", "Projects",
            "tasks", "Tasks",
            "no-projects", "There are no projects!",
            "add", "Add new project",
            "delete-modal-title", "Delete project",
            "delete-modal-left", "Cancel",
            "delete-modal-content-template", "Are you sure to delete {project} project?",
            "delete-modal-right", "Ok"));

        TRANSLATIONS.put("project-page",
            Map.of("project", "project",
                "edit", "Edit",
                "save", "Save",
                "tasks", "Tasks",
                "projects", "Projects"));

        TRANSLATIONS.put("create-project", Map.of(
            "title", "New Project",
            "create", "Create"
        ));

        TRANSLATIONS.put("tasks",
            Map.of("title", "Tasks",
                "no-tasks", "There are no tasks!",
                "projects-filter-placeholder", "Comma separated projects",
                "statuses-filter-placeholder", "Comma separated statuses",
                "options", "Options",
                "status", "Status",
                "add", "Add new task"));

        TRANSLATIONS.put("create-task",
            Map.of("title", "New Task",
                "create", "Create"));

        TRANSLATIONS.put("task",
            Map.of("task", "task",
                "edit-title", "edit",
                "edit", "Edit",
                "save", "Save",
                "tasks", "Tasks"));

        TRANSLATIONS.put("user-account",
            Map.of("title", "Account",
                "sign-out", "Sign out"));

        TRANSLATIONS.put("error-page.title", "Something went wrong...");
        TRANSLATIONS.put("error-modal-title", "Something went wrong...");
    }

    public static void register(String key, String message) {
        TRANSLATIONS.put(key, message);
    }

    public static <T extends Throwable> void register(Class<T> key, ExceptionTranslator<T> translator) {
        EXCEPTION_TRANSLATIONS.put(key.getSimpleName(), translator);
    }

    public static String message(String key) {
        var t = TRANSLATIONS.get(key);
        if (t != null) {
            return t.toString();
        }
        var namespaceKey = key.split("\\.", 2);
        if (namespaceKey.length != 2) {
            return key;
        }
        return messages(namespaceKey[0]).getOrDefault(namespaceKey[1], namespaceKey[1]);
    }

    public static Map<String, String> messages(String key) {
        return (Map<String, String>) TRANSLATIONS.getOrDefault(key, Map.of());
    }

    public static Map<String, Object> enrich(Map<String, Object> params, String prefix) {
        var enriched = new HashMap<>(params);
        messages(prefix).forEach((k, v) -> enriched.put(prefix + "." + k, v));
        return enriched;
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

    public static Map<String, Object> enrichedParams(Map<String, Object> params, String key) {
        var enriched = new HashMap<>(params);
        Optional.ofNullable(TRANSLATIONS.get(key))
            .ifPresent(t -> {
                if (t instanceof Map<?, ?> o) {
                    // TODO: safe!
                    enriched.putAll((Map<String, Object>) o);
                }
            });
        return enriched;
    }

    public interface ExceptionTranslator<T extends Throwable> {
        String translate(T exception);
    }
}
