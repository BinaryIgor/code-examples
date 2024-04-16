package com.binaryigor.htmxproductionsetup.auth;

import com.binaryigor.htmxproductionsetup.shared.contracts.AuthApi;
import com.binaryigor.htmxproductionsetup.shared.contracts.AuthToken;
import com.binaryigor.htmxproductionsetup.shared.contracts.AuthUserApi;
import com.binaryigor.htmxproductionsetup.shared.contracts.UserData;
import com.binaryigor.htmxproductionsetup.shared.exception.UnauthenticatedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TheAuthApi implements AuthApi, AuthUserApi {

    private final AuthTokenCreator authTokenCreator;

    public TheAuthApi(AuthTokenCreator authTokenCreator) {
        this.authTokenCreator = authTokenCreator;
    }

    @Override
    public AuthToken ofUser(UUID id) {
        return authTokenCreator.ofUser(id);
    }

    @Override
    public UUID currentId() {
        return currentUserData().id();
    }

    @Override
    public UserData currentUserData() {
        return AuthenticatedUserRequestHolder.get()
                .orElseThrow(UnauthenticatedException::new);
    }
}
