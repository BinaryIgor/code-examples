package com.binaryigor.htmxproductionsetup.auth;

import com.binaryigor.htmxproductionsetup.shared.AuthToken;

public interface AuthTokenAuthenticator {
    AuthenticationResult authenticate(String token);

    AuthToken refresh(String token);
}
