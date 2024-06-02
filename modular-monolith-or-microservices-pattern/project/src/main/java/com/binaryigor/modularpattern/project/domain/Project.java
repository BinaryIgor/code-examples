package com.binaryigor.modularpattern.project.domain;

import java.util.List;
import java.util.UUID;

public record Project(UUID id,
                      String name,
                      String description,
                      List<UUID> userIds) {
}
