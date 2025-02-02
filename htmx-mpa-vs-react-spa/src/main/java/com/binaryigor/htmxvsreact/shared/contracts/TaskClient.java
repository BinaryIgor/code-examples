package com.binaryigor.htmxvsreact.shared.contracts;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface TaskClient {
    Map<UUID, Integer> tasksCountOfProjects(Collection<UUID> projectIds);
}
