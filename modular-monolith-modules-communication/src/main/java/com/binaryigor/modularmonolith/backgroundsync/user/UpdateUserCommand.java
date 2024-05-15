package com.binaryigor.modularmonolith.backgroundsync.user;

import java.util.UUID;

public record UpdateUserCommand(UUID id, String name, String email) {
}
