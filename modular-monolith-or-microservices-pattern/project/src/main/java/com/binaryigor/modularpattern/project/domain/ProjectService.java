package com.binaryigor.modularpattern.project.domain;

import com.binaryigor.modularpattern.project.domain.exception.ProjectDoesNotExistException;
import com.binaryigor.modularpattern.project.domain.exception.ProjectIdTakenException;
import com.binaryigor.modularpattern.project.domain.exception.ProjectUsersDoNotExistException;

import java.util.UUID;

//TODO: basic name/description validation
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectUserRepository projectUserRepository) {
        this.projectRepository = projectRepository;
        this.projectUserRepository = projectUserRepository;
    }

    public void create(Project project) {
        if (projectRepository.ofId(project.id()).isPresent()) {
            throw new ProjectIdTakenException();
        }
        validateProjectUsersExist(project);
        projectRepository.save(project);
    }

    private void validateProjectUsersExist(Project project) {
        var users = projectUserRepository.ofIds(project.userIds());
        var lackingUsers = project.userIds().stream().filter(uid -> !users.containsKey(uid)).toList();
        if (!lackingUsers.isEmpty()) {
            throw ProjectUsersDoNotExistException.ofIds(lackingUsers);
        }
    }

    public void update(Project project) {
        if (projectRepository.ofId(project.id()).isEmpty()) {
            throw ProjectDoesNotExistException.ofId(project.id());
        }
        validateProjectUsersExist(project);
        projectRepository.save(project);
    }

    public ProjectWithUsers ofId(UUID id) {
        return projectRepository.ofId(id)
            .map(p -> {
                var projectUsers = projectUserRepository.ofIds(p.userIds()).values();
                return new ProjectWithUsers(p, projectUsers);
            })
            .orElseThrow(() -> ProjectDoesNotExistException.ofId(id));
    }
}
