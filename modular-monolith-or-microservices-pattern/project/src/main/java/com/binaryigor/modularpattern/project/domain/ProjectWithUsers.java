package com.binaryigor.modularpattern.project.domain;

import java.util.List;
import java.util.UUID;

public record ProjectWithUsers(UUID id,
                               String namespace,
                               String name,
                               String description,
                               List<ProjectUser> users) {

    public ProjectWithUsers(Project project, List<ProjectUser> users) {
        this(project.id(), project.namespace(), project.name(), project.description(), users);
    }
}
