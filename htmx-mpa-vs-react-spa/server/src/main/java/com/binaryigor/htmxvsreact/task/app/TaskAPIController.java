package com.binaryigor.htmxvsreact.task.app;

import com.binaryigor.htmxvsreact.shared.contracts.ProjectClient;
import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import com.binaryigor.htmxvsreact.task.domain.*;
import com.binaryigor.htmxvsreact.task.domain.exception.TaskStatusValidationException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskAPIController {

    private final TaskService taskService;
    private final UserClient userClient;
    private final ProjectClient projectClient;

    public TaskAPIController(TaskService taskService, UserClient userClient, ProjectClient projectClient) {
        this.taskService = taskService;
        this.userClient = userClient;
        this.projectClient = projectClient;
    }

    @GetMapping
    TasksSearchResult tasks(@RequestParam(required = false, name = "project") List<String> projectNames,
                            @RequestParam(required = false, name = "status") List<String> statuses) {
        return taskService.search(projectNames, taskStatusesFilter(statuses),
            userClient.currentUserId());
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

    @PostMapping
    Task create(@RequestBody CreateTaskRequest request) {
        return taskService.create(new CreateTaskCommand(request.name(), request.project(), userClient.currentUserId()));
    }

    @GetMapping("/{id}")
    EnrichedTask get(@PathVariable("id") UUID id) {
        var task = taskService.get(id, userClient.currentUserId());
        var project = projectClient.ofId(task.projectId());

        return new EnrichedTask(task, project.name());
    }

    @PutMapping("{id}")
    Task update(@PathVariable("id") UUID id, @RequestBody UpdateTaskRequest request) {
        var command = new UpdateTaskCommand(id, request.name(), request.project(),
            TaskStatus.fromString(request.status()), userClient.currentUserId());
        return taskService.update(command);
    }

    @DeleteMapping("{id}")
    void delete(@PathVariable("id") UUID id) {
        taskService.delete(id, userClient.currentUserId());
    }

    record CreateTaskRequest(String name, String project) {
    }

    record UpdateTaskRequest(String name, String project, String status) {
    }

    record EnrichedTask(Task task, String projectName) {
    }
}
