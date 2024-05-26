package com.binaryigor.modularpattern.project.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectUserRepository projectUserRepository) {
        this.projectRepository = projectRepository;
        this.projectUserRepository = projectUserRepository;
    }

    public void create(Project project) {
        //TODO: validate
        projectRepository.save(project);
    }

    public void update(Project project) {
        //TODO: validate
        projectRepository.save(project);
    }

    public ProjectWithUsers ofId(UUID id) {
        return projectRepository.ofId(id)
            .map(p -> {
                var projectUsers = new ArrayList<>(projectUserRepository.ofIds(p.userIds()).values());
                return new ProjectWithUsers(p, projectUsers);
            })
            .orElseThrow(() -> ProjectDoesNotExistException.ofId(id));
    }

    public List<ProjectWithUsers> allOfNamespace(String namespace) {
        var projectsWithoutUsers = projectRepository.allOfNamespace(namespace);

        var projectsUserIds = projectsWithoutUsers.stream()
            .flatMap(p -> p.userIds().stream())
            .collect(Collectors.toSet());

        var usersByIds = projectUserRepository.ofIds(projectsUserIds);

        return projectsWithoutUsers.stream()
            .map(p -> {
                var users = p.userIds().stream()
                    //TODO: throw exception?
                    .map(usersByIds::get)
                    .toList();

                return new ProjectWithUsers(p.id(), p.namespace(), p.name(), p.description(), users);
            })
            .toList();
    }
}
