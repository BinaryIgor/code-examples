package com.binaryigor.htmxvsreact.project;

import com.binaryigor.htmxvsreact.project.exception.ProjectDoestNotExistException;
import com.binaryigor.htmxvsreact.project.exception.ProjectNameConflictException;
import com.binaryigor.htmxvsreact.project.exception.ProjectOwnerException;

import java.util.UUID;

public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
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
            .orElseThrow(() -> new ProjectDoestNotExistException("Project of %s id doesn't exist".formatted(projectId)));

        if (!project.ownerId().equals(userId)) {
            throw ProjectOwnerException.ofCurrentUser(userId);
        }

        return project;
    }
}
