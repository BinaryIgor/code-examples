package com.binaryigor.htmxvsreact.user.domain;

import com.binaryigor.htmxvsreact.shared.AppLanguage;
import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import com.binaryigor.htmxvsreact.user.infra.web.CurrentRequestUser;

import java.util.Optional;
import java.util.UUID;

public class TheUserClient implements UserClient {

    @Override
    public Optional<UUID> currentUserIdOpt() {
        return CurrentRequestUser.idOpt();
    }

    @Override
    public UUID currentUserId() {
        return CurrentRequestUser.id();
    }

    @Override
    public AppLanguage currentLanguage() {
        return CurrentRequestUser.language();
    }
}
