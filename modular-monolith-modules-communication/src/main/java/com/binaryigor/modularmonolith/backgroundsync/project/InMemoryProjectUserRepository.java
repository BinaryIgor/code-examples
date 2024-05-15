package com.binaryigor.modularmonolith.backgroundsync.project;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryProjectUserRepository implements ProjectUserRepository {

    private final Map<UUID, ProjectUser> db = new ConcurrentHashMap<>();

    @Override
    public void save(ProjectUser user) {
        db.put(user.id(), user);
    }

    @Override
    public void saveAll(Collection<ProjectUser> users) {
        users.forEach(this::save);
    }

    @Override
    public Map<UUID, ProjectUser> ofIds(List<UUID> userIds) {
        return db.entrySet().stream()
                .filter(e -> userIds.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
