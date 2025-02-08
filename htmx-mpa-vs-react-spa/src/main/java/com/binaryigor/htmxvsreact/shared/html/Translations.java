package com.binaryigor.htmxvsreact.shared.html;

import com.binaryigor.htmxvsreact.shared.AppLanguage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.binaryigor.htmxvsreact.shared.AppLanguage.EN;
import static java.util.Map.entry;

public class Translations {

    private final Map<AppLanguage, Map<String, Object>> translations = new ConcurrentHashMap<>();
    @SuppressWarnings("rawtypes")
    private final Map<String, ExceptionTranslator> exceptionTranslations = new ConcurrentHashMap<>();
    private final Supplier<AppLanguage> currentLanguage;

    public Translations(Supplier<AppLanguage> currentLanguage) {
        this.currentLanguage = currentLanguage;
        registerTranslations();
    }

    private void registerTranslations() {
        var enTranslations = new ConcurrentHashMap<String, Object>();
        enTranslations.put("sign-in",
            Map.of("title", "Sign In",
                "email-placeholder", "Email",
                "password-placeholder", "Password",
                "sign-in", "Sign in"));

        enTranslations.put("navigation",
            Map.of("projects", "Projects",
                "tasks", "Tasks",
                "account", "Account"));

        enTranslations.put("projects", Map.of(
            "title", "Projects",
            "tasks", "Tasks",
            "no-projects", "There are no projects!",
            "add", "Add new project",
            "delete-modal-title", "Delete project",
            "delete-modal-left", "Cancel",
            "delete-modal-content-template", "Are you sure to delete {project} project?",
            "delete-modal-right", "Ok"));

        enTranslations.put("project-page",
            Map.of("project", "project",
                "edit", "Edit",
                "name-placeholder", "Name",
                "save", "Save",
                "tasks", "Tasks",
                "projects", "Projects"));

        enTranslations.put("create-project", Map.of(
            "title", "New Project",
            "name-placeholder", "Name",
            "create", "Create"
        ));

        enTranslations.put("tasks",
            Map.ofEntries(entry("title", "Tasks"),
                entry("no-tasks", "There are no tasks!"),
                entry("projects-filter-placeholder", "Comma-separated projects"),
                entry("statuses-filter-placeholder", "Comma-separated statuses"),
                entry("delete-task-modal-title", "Delete task"),
                entry("delete-task-modal-message-template", "Are you sure to delete {task} task?"),
                entry("delete-task-modal-left", "Cancel"),
                entry("delete-task-modal-right", "Ok"),
                entry("options", "Options"),
                entry("status", "Status"),
                entry("add", "Add new task"),
                entry("no-projects", "There are no projects! Create one first.")));

        enTranslations.put("create-task",
            Map.of("title", "New Task",
                "name-placeholder", "Name",
                "project-options", "Options",
                "project-placeholder", "Project",
                "create", "Create"));

        enTranslations.put("task",
            Map.ofEntries(entry("task", "task"),
                entry("edit-title", "edit"),
                entry("edit", "Edit"),
                entry("name-placeholder", "Name"),
                entry("project-options", "Options"),
                entry("project-placeholder", "Project"),
                entry("status-options", "Options"),
                entry("status-placeholder", "Status"),
                entry("project-label", "Project"),
                entry("status-label", "Status"),
                entry("save", "Save"),
                entry("tasks", "Tasks")));

        enTranslations.put("user-account",
            Map.of("title", "Account",
                "name-label", "Name",
                "email-label", "Email",
                "language-label", "Language",
                "sign-out", "Sign out"));

        enTranslations.put("error-page.title", "Something went wrong...");
        enTranslations.put("error-modal-title", "Something went wrong...");

        translations.put(EN, enTranslations);
    }

    public <T extends Throwable> void register(Class<T> key, ExceptionTranslator<T> translator) {
        exceptionTranslations.put(key.getSimpleName(), translator);
    }

    public String message(String key) {
        var t = translations.get(currentLanguage.get()).get(key);
        if (t != null) {
            return t.toString();
        }
        var namespaceKey = key.split("\\.", 2);
        if (namespaceKey.length != 2) {
            return key;
        }
        return messages(namespaceKey[0]).getOrDefault(namespaceKey[1], namespaceKey[1]);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> messages(String key) {
        return (Map<String, String>) translations.get(currentLanguage.get()).getOrDefault(key, Map.of());
    }

    public Map<String, Object> enrich(Map<String, Object> params, String prefix) {
        var enriched = new HashMap<>(params);
        messages(prefix).forEach((k, v) -> enriched.put(prefix + "." + k, v));
        return enriched;
    }

    @SuppressWarnings("unchecked")
    public String error(Throwable key) {
        return exceptionTranslations.computeIfAbsent(key.getClass().getSimpleName(), k -> (l, e) -> key.getMessage())
            .translate(currentLanguage.get(), key);
    }

    @SuppressWarnings("unchecked")
    public String error(Class<? extends Throwable> key) {
        return exceptionTranslations.computeIfAbsent(key.getSimpleName(), k -> (l, e) -> key.getSimpleName())
            .translate(currentLanguage.get(), null);
    }

    public interface ExceptionTranslator<T extends Throwable> {
        String translate(AppLanguage language, T exception);
    }
}
