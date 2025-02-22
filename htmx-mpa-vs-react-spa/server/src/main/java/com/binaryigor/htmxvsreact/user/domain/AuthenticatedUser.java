package com.binaryigor.htmxvsreact.user.domain;

import com.binaryigor.htmxvsreact.shared.AppLanguage;

import java.util.UUID;

public record AuthenticatedUser(UUID id, AppLanguage language) {
}
