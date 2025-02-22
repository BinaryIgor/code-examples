package com.binaryigor.htmxvsreact.task.domain;

import java.util.UUID;

public record CreateTaskCommand(String name, String project, UUID ownerId) {
}
