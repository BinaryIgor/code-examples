package com.binaryigor.htmxvsreact.task.app;

import com.binaryigor.htmxvsreact.shared.HTMX;
import com.binaryigor.htmxvsreact.shared.contracts.ProjectClient;
import com.binaryigor.htmxvsreact.shared.contracts.ProjectView;
import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import com.binaryigor.htmxvsreact.shared.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.shared.html.Translations;
import com.binaryigor.htmxvsreact.task.domain.CreateTaskCommand;
import com.binaryigor.htmxvsreact.task.domain.TaskService;
import com.binaryigor.htmxvsreact.task.domain.TaskStatus;
import com.binaryigor.htmxvsreact.task.domain.UpdateTaskCommand;
import com.binaryigor.htmxvsreact.task.domain.exception.TaskNameValidationException;
import com.binaryigor.htmxvsreact.task.domain.exception.TaskProjectValidationException;
import com.binaryigor.htmxvsreact.task.domain.exception.TaskStatusValidationException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@Hidden
@RestController
@RequestMapping("/tasks")
public class TaskHTMLController {

    private final HTMLTemplates templates;
    private final Translations translations;
    private final TaskService taskService;
    private final UserClient userClient;
    private final ProjectClient projectClient;

    public TaskHTMLController(HTMLTemplates templates,
                              Translations translations,
                              TaskService taskService,
                              UserClient userClient,
                              ProjectClient projectClient) {
        this.templates = templates;
        this.translations = translations;
        this.taskService = taskService;
        this.userClient = userClient;
        this.projectClient = projectClient;
    }

    @GetMapping
    String tasks(@RequestParam(required = false, name = "project") List<String> projectNames,
                 @RequestParam(required = false, name = "status") List<String> statuses) {
        var searchResults = taskService.search(projectNames, taskStatusesFilter(statuses),
            userClient.currentUserId());

        var pageParams = translations.enrich(Map.ofEntries(
                entry("title", translations.message("tasks.title")),
                entry("projectsFilterValue", projectNames == null ? "" : String.join(",", projectNames)),
                entry("projectsFilterOptions", String.join(", ", searchResults.availableProjects())),
                entry("statusesFilterValue", statuses == null ? "" : String.join(",", statuses)),
                entry("statusesFilterOptions", taskStatusAllowedValuesString()),
                entry("hasProjects", !searchResults.availableProjects().isEmpty()),
                entry("tasks", searchResults.tasks()),
                entry("confirmableModalId", "delete-task-modal"),
                entry("confirmableModalTitle", translations.message("tasks.delete-task-modal-title")),
                entry("confirmableModalLeft", translations.message("tasks.delete-task-modal-left")),
                entry("confirmableModalRight", translations.message("tasks.delete-task-modal-right"))),
            "tasks");

        if (HTMX.isHTMXRequest()) {
            return templates.render("task/tasks-search-results.mustache", pageParams);
        }
        return templates.renderPage("task/tasks.mustache", pageParams);
    }

    private Collection<TaskStatus> taskStatusesFilter(List<String> statuses) {
        if (statuses == null) {
            return Set.of();
        }
        return statuses.stream().map(s -> {
                try {
                    return TaskStatus.fromString(s);
                } catch (TaskStatusValidationException e) {
                    return null;
                }
            }).filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private String taskStatusAllowedValuesString() {
        return Arrays.stream(TaskStatus.values()).map(Enum::name).collect(Collectors.joining(", "));
    }

    @GetMapping("/create")
    String createTaskPage(@RequestParam(required = false) String tasksSearch) {
        var allUserProjects = allUserProjectNames(userClient.currentUserId());
        var projectErrorMessage = translations.error(new TaskProjectValidationException(allUserProjects));
        return templates.renderPage("task/create-task.mustache",
            translations.enrich(Map.ofEntries(
                    entry("title", translations.message("create-task.title")),
                    entry("taskFormAttrs", "hx-post='%s'".formatted(urlWithRawTasksSearchIfPresent("/tasks", tasksSearch))),
                    entry("namePlaceholder", translations.message("create-task.name-placeholder")),
                    entry("nameValue", ""),
                    entry("nameError", translations.error(TaskNameValidationException.class)),
                    entry("projectPlaceholder", translations.message("create-task.project-placeholder")),
                    entry("projectValue", ""),
                    entry("projectOptions", translations.message("create-task.project-options")),
                    entry("projectAllowedValues", userProjectsString(allUserProjects)),
                    entry("projectError", projectErrorMessage),
                    entry("submitValue", translations.message("create-task.create"))),
                "create-task"));
    }

    private String urlWithRawTasksSearchIfPresent(String url, String tasksSearch) {
        return tasksSearch == null || tasksSearch.isEmpty() ? url : url + "?tasksSearch=" + tasksSearch;
    }

    private Collection<String> allUserProjectNames(UUID userId) {
        return projectClient.allOfOwner(userId).stream().map(ProjectView::name).toList();
    }

    private String userProjectsString(Collection<String> userProjects) {
        return String.join(", ", userProjects);
    }

    @PostMapping
    ResponseEntity<?> createTask(@RequestParam String name,
                                 @RequestParam String project,
                                 @RequestParam(required = false) String tasksSearch) {
        taskService.create(new CreateTaskCommand(name, project, userClient.currentUserId()));
        return ResponseEntity.status(HttpStatus.CREATED)
            .header(HTMX.REDIRECT_HEADER, tasksPageUrlWithDecodedSearchIfPresent(tasksSearch))
            .build();
    }

    private String tasksPageUrlWithDecodedSearchIfPresent(String tasksSearch) {
        return urlWithRawTasksSearchIfPresent("/tasks", fromBase64(tasksSearch));
    }

    @GetMapping("/{id}")
    String taskPage(@PathVariable("id") UUID id, @RequestParam(required = false) String tasksSearch) {
        var task = taskService.get(id, userClient.currentUserId());
        var projectName = projectClient.ofId(task.projectId()).name();
        var allUserProjects = allUserProjectNames(userClient.currentUserId());
        var projectErrorMessage = translations.error(new TaskProjectValidationException(allUserProjects));

        return templates.renderPage("task/task.mustache",
            translations.enrich(Map.ofEntries(
                    entry("title", "%s %s".formatted(task.name(), translations.message("task.task"))),
                    entry("taskFormAttrs", "hx-put='%s'".formatted(urlWithRawTasksSearchIfPresent("/tasks/" + id, tasksSearch))),
                    entry("namePlaceholder", translations.message("task.name-placeholder")),
                    entry("nameValue", task.name()),
                    entry("nameError", translations.error(TaskNameValidationException.class)),
                    entry("projectPlaceholder", translations.message("task.project-placeholder")),
                    entry("projectValue", projectName),
                    entry("projectOptions", translations.message("task.project-options")),
                    entry("projectAllowedValues", userProjectsString(allUserProjects)),
                    entry("projectError", projectErrorMessage),
                    entry("statusOptions", translations.message("task.status-options")),
                    entry("statusPlaceholder", translations.message("task.status-placeholder")),
                    entry("statusValue", task.status()),
                    entry("statusAllowedValues", taskStatusAllowedValuesString()),
                    entry("statusError", translations.error(TaskStatusValidationException.class)),
                    entry("submitValue", translations.message("task.save")),
                    entry("task", task),
                    entry("tasksUrl", "/tasks" + (tasksSearch == null ? "" : ("?" + fromBase64(tasksSearch))))),
                "task"));
    }

    private String fromBase64(String string) {
        if (string == null || string.isBlank()) {
            return string;
        }
        return new String(Base64.getDecoder().decode(string), StandardCharsets.UTF_8);
    }

    @PutMapping("{id}")
    ResponseEntity<?> updateTask(@PathVariable("id") UUID id,
                                 @RequestParam String name,
                                 @RequestParam String project,
                                 @RequestParam String status,
                                 @RequestParam(required = false) String tasksSearch) {
        taskService.update(new UpdateTaskCommand(id, name, project, TaskStatus.fromString(status),
            userClient.currentUserId()));
        return ResponseEntity.ok()
            .header(HTMX.REDIRECT_HEADER, urlWithRawTasksSearchIfPresent("/tasks/" + id, tasksSearch))
            .build();
    }

    @DeleteMapping("{id}")
    void delete(@PathVariable("id") UUID id) {
        taskService.delete(id, userClient.currentUserId());
    }
}
