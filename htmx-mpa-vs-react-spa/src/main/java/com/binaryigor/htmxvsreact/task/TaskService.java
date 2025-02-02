package com.binaryigor.htmxvsreact.task;

import com.binaryigor.htmxvsreact.shared.contracts.ProjectClient;
import com.binaryigor.htmxvsreact.task.exception.TaskDoestNotExistException;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectClient projectClient;

    public TaskService(TaskRepository taskRepository, ProjectClient projectClient) {
        this.taskRepository = taskRepository;
        this.projectClient = projectClient;
    }

    public void create(CreateTaskCommand command) {
        var projectId = projectClient.idOfName(command.project());
        var task = new Task(UUID.randomUUID(), command.name(), projectId, TaskStatus.TODO);
        taskRepository.save(task);
    }

    // TODO: support project change!
    public Task update(UpdateTaskCommand command) {
        var task = get(command.id(), command.userId());
        var updatedTask = new Task(command.id(), command.name(), task.projectId(), command.status());
        taskRepository.save(updatedTask);
        return updatedTask;
    }

    // TODO: project id = name
    public List<Task> search(Collection<UUID> projectIds,
                             Collection<TaskStatus> statuses) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        return taskRepository.of(projectIds, statuses);
    }

    public Task get(UUID taskId, UUID userId) {
        var task = taskRepository.ofId(taskId).orElseThrow(() -> new TaskDoestNotExistException("Task of %s id doesn't exist".formatted(taskId)));
        // TODO: verify user access
        return task;
    }
}
