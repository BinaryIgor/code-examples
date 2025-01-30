package com.binaryigor.htmxvsreact.project;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository {

    List<Project> userProjects(UUID userId);

    void save(Project project);

    void delete(UUID id);
}
