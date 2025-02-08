package com.binaryigor.htmxvsreact.project.domain;

import com.binaryigor.htmxvsreact.project.domain.exception.ProjectDoestNotExistException;
import com.binaryigor.htmxvsreact.project.domain.exception.ProjectNameConflictException;
import com.binaryigor.htmxvsreact.project.domain.exception.ProjectOwnerException;
import com.binaryigor.htmxvsreact.shared.contracts.ProjectClient;
import com.binaryigor.htmxvsreact.shared.contracts.ProjectView;
import com.binaryigor.htmxvsreact.shared.contracts.TaskClient;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ProjectService implements ProjectClient {

    private final ProjectRepository projectRepository;
    private final TaskClient taskClient;

    public ProjectService(ProjectRepository projectRepository, TaskClient taskClient) {
        this.projectRepository = projectRepository;
        this.taskClient = taskClient;
    }

    public List<ProjectWithTasks> userProjects(UUID userId) {
        var projects = projectRepository.userProjects(userId);
        var projectsTasks = taskClient.tasksCountOfProjects(projects.stream().map(Project::id).toList());
        return projects.stream().map(p -> new ProjectWithTasks(p, projectsTasks.getOrDefault(p.id(), 0))).toList();
    }

    public void create(Project project) {
        if (projectRepository.ofName(project.name()).isPresent()) {
            throw new ProjectNameConflictException(project.name());
        }
        projectRepository.save(project);
    }

    public void update(Project project) {
        var sameNameProject = projectRepository.ofName(project.name());
        if (sameNameProject.isPresent() && !sameNameProject.get().id().equals(project.id())) {
            throw new ProjectNameConflictException(project.name());
        }

        get(project.id(), project.ownerId());

        projectRepository.save(project);
    }

    public void delete(UUID projectId, UUID userId) {
        projectRepository.ofId(projectId)
            .ifPresent(p -> {
                if (!p.ownerId().equals(userId)) {
                    throw ProjectOwnerException.ofCurrentUser(userId);
                }
                projectRepository.delete(projectId);
            });
    }

    public Project get(UUID projectId, UUID userId) {
        var project = projectRepository.ofId(projectId)
            .orElseThrow(() -> ProjectDoestNotExistException.ofId(projectId));

        if (!project.ownerId().equals(userId)) {
            throw ProjectOwnerException.ofCurrentUser(userId);
        }

        return project;
    }

    @Override
    public Collection<ProjectView> allOfOwner(UUID ownerId) {
        return projectRepository.userProjects(ownerId).stream().map(Project::toView).toList();
    }

    @Override
    public Collection<UUID> idsOfNames(Collection<String> names) {
        return projectRepository.ofNames(names).stream().map(Project::id).toList();
    }

    @Override
    public ProjectView ofId(UUID id) {
        return projectRepository.ofId(id)
            .map(Project::toView)
            .orElseThrow(() -> ProjectDoestNotExistException.ofId(id));
    }

    @Override
    public ProjectView ofName(String name) {
        return projectRepository.ofName(name)
            .map(Project::toView)
            .orElseThrow(() -> ProjectDoestNotExistException.ofName(name));
    }
}
