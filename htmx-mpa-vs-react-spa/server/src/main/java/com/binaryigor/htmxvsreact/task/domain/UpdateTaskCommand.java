package com.binaryigor.htmxvsreact.task.domain;

import java.util.UUID;

public record UpdateTaskCommand(UUID id, String name, String project, TaskStatus status, UUID userId) {
}
