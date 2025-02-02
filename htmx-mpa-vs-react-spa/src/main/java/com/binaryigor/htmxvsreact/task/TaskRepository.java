package com.binaryigor.htmxvsreact.task;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {

    List<Task> of(Collection<UUID> projectIds, Collection<TaskStatus> statuses);

    void save(Task task);

    Optional<Task> ofId(UUID id);
}
