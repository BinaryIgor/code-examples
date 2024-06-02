package com.binaryigor.modularpattern.project.domain;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {

    void save(Project project);

    Optional<Project> ofId(UUID id);
}
