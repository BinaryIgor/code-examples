package com.binaryigor.htmxproductionsetup.auth;

import com.binaryigor.htmxproductionsetup.shared.AuthenticatedUser;
import com.binaryigor.htmxproductionsetup.shared.contracts.*;
import com.binaryigor.htmxproductionsetup.shared.exception.UnauthenticatedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TheAuthApi implements AuthApi, AuthUserApi {

    private final AuthTokenCreator authTokenCreator;
    private final UserApi userApi;

    public TheAuthApi(AuthTokenCreator authTokenCreator,
                      UserApi userApi) {
        this.authTokenCreator = authTokenCreator;
        this.userApi = userApi;
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

    @Override
    public UserData currentUserData() {
        return userApi.userOfId(currentId());
    }
}
