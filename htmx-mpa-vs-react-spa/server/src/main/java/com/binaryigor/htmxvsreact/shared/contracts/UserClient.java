package com.binaryigor.htmxvsreact.shared.contracts;

import com.binaryigor.htmxvsreact.shared.AppLanguage;

import java.util.Optional;
import java.util.UUID;

public interface UserClient {

    UUID currentUserId();

    Optional<UUID> currentUserIdOpt();

    AppLanguage currentLanguage();
}
