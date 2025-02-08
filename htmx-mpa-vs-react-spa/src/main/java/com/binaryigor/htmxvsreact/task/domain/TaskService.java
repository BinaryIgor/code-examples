package com.binaryigor.htmxvsreact.task.domain;

import com.binaryigor.htmxvsreact.shared.contracts.ProjectClient;
import com.binaryigor.htmxvsreact.shared.contracts.ProjectView;
import com.binaryigor.htmxvsreact.task.domain.exception.TaskDoestNotExistException;
import com.binaryigor.htmxvsreact.task.domain.exception.TaskOwnerException;
import com.binaryigor.htmxvsreact.task.domain.exception.TaskProjectOwnerException;

import java.util.Collection;
import java.util.UUID;

public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectClient projectClient;

    public TaskService(TaskRepository taskRepository, ProjectClient projectClient) {
        this.taskRepository = taskRepository;
        this.projectClient = projectClient;
    }

    public TasksSearchResult search(Collection<String> projectNames,
                                    Collection<TaskStatus> statuses,
                                    UUID userId) {
        var userProjects = projectClient.allOfOwner(userId);
        Collection<UUID> projectIds;

        if (projectNames == null || projectNames.isEmpty()) {
            projectIds = userProjects.stream().map(ProjectView::id).toList();
        } else {
            projectIds = projectClient.idsOfNames(projectNames);
        }

        if (projectIds == null || projectIds.isEmpty()) {
            return TasksSearchResult.empty();
        }

        var tasks = taskRepository.of(projectIds, statuses);
        var availableProjects = userProjects.stream().map(ProjectView::name).toList();
        return new TasksSearchResult(tasks, availableProjects);
    }

    public Task create(CreateTaskCommand command) {
        var project = projectClient.ofName(command.project());
        validateProjectOwnedByUser(project, command.ownerId());

        var task = new Task(UUID.randomUUID(), command.name(), project.id(), command.ownerId(), TaskStatus.TODO);
        taskRepository.save(task);
        return task;
    }

    private void validateProjectOwnedByUser(ProjectView project, UUID userId) {
        if (!project.ownerId().equals(userId)) {
            throw TaskProjectOwnerException.ofCurrentUser(userId);
        }
    }

    public Task update(UpdateTaskCommand command) {
        get(command.id(), command.userId());

        var project = projectClient.ofName(command.project());
        validateProjectOwnedByUser(project, command.userId());

        var updatedTask = new Task(command.id(), command.name(), project.id(), command.userId(), command.status());
        taskRepository.save(updatedTask);
        return updatedTask;
    }

    public Task get(UUID taskId, UUID userId) {
        var task = taskRepository.ofId(taskId).orElseThrow(() -> TaskDoestNotExistException.ofId(taskId));
        if (!task.ownerId().equals(userId)) {
            throw TaskOwnerException.ofCurrentUser(userId);
        }
        return task;
    }

    public void delete(UUID taskId, UUID userId) {
        get(taskId, userId);
        taskRepository.delete(taskId);
    }
}
