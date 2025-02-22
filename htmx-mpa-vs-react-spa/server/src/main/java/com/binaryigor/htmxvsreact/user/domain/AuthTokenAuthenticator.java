package com.binaryigor.htmxvsreact.user.domain;

public interface AuthTokenAuthenticator {

    AuthenticationResult authenticate(String token);

    AuthToken refresh(String token);
}
