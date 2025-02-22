package com.binaryigor.htmxvsreact.task.domain;

import java.util.Collection;
import java.util.List;

public record TasksSearchResult(Collection<Task> tasks, Collection<String> availableProjects) {

    public static TasksSearchResult empty() {
        return new TasksSearchResult(List.of(), List.of());
    }
}
