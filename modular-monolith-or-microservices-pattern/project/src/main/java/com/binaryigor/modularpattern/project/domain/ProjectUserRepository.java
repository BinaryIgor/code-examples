package com.binaryigor.modularpattern.project.domain;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface ProjectUserRepository {

    void save(Collection<ProjectUser> users);

    Map<UUID, ProjectUser> ofIds(Collection<UUID> ids);

    Optional<ProjectUser> ofId(UUID id);

    Stream<ProjectUser> allUsers();
}
