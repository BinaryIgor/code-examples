package com.binaryigor.modularmonolith.backgroundsync.project;

import java.util.*;

public interface ProjectUserRepository {

    void save(ProjectUser user);

    void saveAll(Collection<ProjectUser> users);

    Map<UUID, ProjectUser> ofIds(List<UUID> userIds);
}
