package com.binaryigor.htmxvsreact.user.domain;

import java.util.UUID;

public interface AuthTokenCreator {
    AuthToken ofUser(UUID id);
}
