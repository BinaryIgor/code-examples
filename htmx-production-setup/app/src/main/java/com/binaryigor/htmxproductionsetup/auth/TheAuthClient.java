package com.binaryigor.htmxproductionsetup.auth;

import com.binaryigor.htmxproductionsetup.shared.AuthToken;
import com.binaryigor.htmxproductionsetup.shared.AuthenticatedUser;
import com.binaryigor.htmxproductionsetup.shared.contracts.AuthClient;
import com.binaryigor.htmxproductionsetup.shared.contracts.AuthUserClient;
import com.binaryigor.htmxproductionsetup.shared.exception.UnauthenticatedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TheAuthClient implements AuthClient, AuthUserClient {

    private final AuthTokenCreator authTokenCreator;

    public TheAuthClient(AuthTokenCreator authTokenCreator) {
        this.authTokenCreator = authTokenCreator;
    }

    @Override
    public AuthToken ofUser(UUID id) {
        return authTokenCreator.ofUser(id);
    }

    @Override
    public UUID currentId() {
        return AuthenticatedUserRequestHolder.get()
                .map(AuthenticatedUser::id)
                .orElseThrow(UnauthenticatedException::new);
    }
}
