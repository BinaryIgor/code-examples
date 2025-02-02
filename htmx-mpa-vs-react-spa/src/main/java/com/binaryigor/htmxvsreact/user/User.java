package com.binaryigor.htmxvsreact.user;

import java.util.UUID;

public record User(UUID id, String email, String name, String password) {
}
