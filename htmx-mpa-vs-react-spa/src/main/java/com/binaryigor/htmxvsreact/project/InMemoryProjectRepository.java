package com.binaryigor.htmxvsreact.project;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProjectRepository implements ProjectRepository {

    private final Map<UUID, Project> db = new ConcurrentHashMap<>();

    @Override
    public List<Project> userProjects(UUID userId) {
        return db.values().stream()
            .filter(p -> p.ownerId().equals(userId))
            .toList();
    }

    @Override
    public void save(Project project) {
        db.put(project.id(), project);
    }

    @Override
    public void delete(UUID id) {
        db.remove(id);
    }
}
