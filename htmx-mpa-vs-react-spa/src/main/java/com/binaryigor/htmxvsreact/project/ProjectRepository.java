package com.binaryigor.htmxvsreact.project;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {

    List<Project> userProjects(UUID userId);

    Optional<Project> ofName(String name);

    List<Project> ofNames(Collection<String> names);

    Optional<Project> ofId(UUID id);

    void save(Project project);

    void delete(UUID id);
}
