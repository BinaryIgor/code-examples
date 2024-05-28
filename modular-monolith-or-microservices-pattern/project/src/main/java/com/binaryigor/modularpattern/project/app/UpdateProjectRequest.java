package com.binaryigor.modularpattern.project.app;

import com.binaryigor.modularpattern.project.domain.Project;

import java.util.List;
import java.util.UUID;

public record UpdateProjectRequest(String name,
                                   String description,
                                   List<UUID> userIds) {

    public Project toProject(UUID id) {
        return new Project(id, name, description, userIds);
    }
}
