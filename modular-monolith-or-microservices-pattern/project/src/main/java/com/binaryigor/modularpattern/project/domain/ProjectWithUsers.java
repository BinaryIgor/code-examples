package com.binaryigor.modularpattern.project.domain;

import java.util.Collection;
import java.util.UUID;

public record ProjectWithUsers(UUID id,
                               String name,
                               String description,
                               Collection<ProjectUser> users) {

    public ProjectWithUsers(Project project, Collection<ProjectUser> users) {
        this(project.id(), project.name(), project.description(), users);
    }
}
