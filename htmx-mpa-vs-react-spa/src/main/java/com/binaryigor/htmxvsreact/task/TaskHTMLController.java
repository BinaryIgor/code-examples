package com.binaryigor.htmxvsreact.task;

import com.binaryigor.htmxvsreact.shared.HTMX;
import com.binaryigor.htmxvsreact.shared.Translations;
import com.binaryigor.htmxvsreact.shared.contracts.ProjectClient;
import com.binaryigor.htmxvsreact.shared.contracts.ProjectView;
import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import com.binaryigor.htmxvsreact.shared.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.task.exception.TaskNameValidationException;
import com.binaryigor.htmxvsreact.task.exception.TaskProjectValidationException;
import com.binaryigor.htmxvsreact.task.exception.TaskStatusValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@RestController
@RequestMapping("/tasks")
public class TaskHTMLController {

    private final HTMLTemplates templates;
    private final TaskService taskService;
    private final UserClient userClient;
    private final ProjectClient projectClient;

    public TaskHTMLController(HTMLTemplates templates,
                              TaskService taskService,
                              UserClient userClient,
                              ProjectClient projectClient) {
        this.templates = templates;
        this.taskService = taskService;
        this.userClient = userClient;
        this.projectClient = projectClient;
    }

    // TODO: status filter
    @GetMapping
    String tasks(@RequestParam(required = false, name = "projectName") List<String> projectNames,
                 @RequestParam(required = false, name = "status") List<String> statuses) {
        var userId = userClient.currentUserId();
        // TODO: validate owner!
        var userProjects = projectClient.allOfOwner(userId);
        Collection<UUID> projectIds;
        if (projectNames == null || projectNames.isEmpty()) {
            projectIds = userProjects.stream().map(ProjectView::id).toList();
        } else {
            projectIds = projectClient.idsOfNames(projectNames);
        }
        var tasks = taskService.search(projectIds, taskStatusesFilter(statuses));

        var pageParams = Translations.enrich(Map.of(
                "title", Translations.message("tasks.title"),
                "projectsFilterValue", projectNames == null ? "" : String.join(",", projectNames),
                "projectsFilterOptions", userProjects.stream().map(ProjectView::name).collect(Collectors.joining(", ")),
                "statusesFilterValue", statuses == null ? "" : String.join(",", statuses),
                "statusesFilterOptions", Arrays.stream(TaskStatus.values()).map(Enum::name).collect(Collectors.joining(", ")),
                "tasks", tasks),
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

    @GetMapping("/create")
    String createTaskPage(@RequestParam(required = false) String tasksSearch) {
        return templates.renderPage("task/create-task.mustache",
            Translations.enrich(Map.of("title", Translations.message("create-task.title"),
                    "taskFormAttrs", "hx-post='%s'".formatted(urlWithRawTasksSearchIfPresent("/tasks", tasksSearch)),
                    "nameValue", "",
                    "nameErrorHidden", true,
                    "nameError", Translations.error(TaskNameValidationException.class),
                    "projectValue", "",
                    "projectErrorHidden", true,
                    "projectError", Translations.error(TaskProjectValidationException.class),
                    "submitValue", Translations.message("create-task.create")),
                "create-task"));
    }

    private String urlWithRawTasksSearchIfPresent(String url, String tasksSearch) {
        return tasksSearch == null || tasksSearch.isEmpty() ? url : url + "?tasksSearch=" + tasksSearch;
    }

    @PostMapping
    ResponseEntity<?> createTask(@RequestParam String name,
                                 @RequestParam String project,
                                 @RequestParam(required = false) String tasksSearch) {
        taskService.create(new CreateTaskCommand(name, project));
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
        // TODO: resign from hidden flags since errors are always hidden when initially rendered?

        return templates.renderPage("task/task.mustache",
            Translations.enrich(Map.ofEntries(
                    entry("title", "%s %s".formatted(task.name(), Translations.message("task.task"))),
                    entry("taskFormAttrs", "hx-put='%s'".formatted(urlWithRawTasksSearchIfPresent("/tasks/" + id, tasksSearch))),
                    entry("nameValue", task.name()),
                    entry("nameErrorHidden", true),
                    entry("nameError", Translations.error(TaskNameValidationException.class)),
                    entry("projectValue", projectName),
                    entry("projectErrorHidden", true),
                    entry("projectError", Translations.error(TaskProjectValidationException.class)),
                    entry("statusValue", task.status()),
                    entry("statusAllowedValues", Arrays.stream(TaskStatus.values()).map(Enum::name).collect(Collectors.joining(","))),
                    entry("statusErrorHidden", true),
                    entry("statusError", Translations.error(TaskStatusValidationException.class)),
                    entry("submitValue", Translations.message("task.save")),
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
}
