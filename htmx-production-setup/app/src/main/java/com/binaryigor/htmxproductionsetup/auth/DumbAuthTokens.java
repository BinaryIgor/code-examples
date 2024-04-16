package com.binaryigor.htmxproductionsetup.auth;

import com.binaryigor.htmxproductionsetup.shared.contracts.AuthToken;
import com.binaryigor.htmxproductionsetup.shared.AuthenticatedUser;
import com.binaryigor.htmxproductionsetup.shared.exception.InvalidAuthTokenException;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

//TODO: remove!
//@Component
public class DumbAuthTokens implements AuthTokenCreator, AuthTokenAuthenticator {

    private static final String SECRET = "secret";
    private static final String TOKEN_PARTS_SEPARATOR = "__";
    private static final Duration AUTH_TOKEN_DURATION = Duration.ofHours(24);
    private final Clock clock;

    public DumbAuthTokens(Clock clock) {
        this.clock = clock;
    }

    @Override
    public AuthenticationResult authenticate(String token) {
        return validateToken(token);
    }

    private AuthenticationResult validateToken(String token) {
        try {
            var decoded = new String(Base64.getUrlDecoder().decode(token.getBytes(StandardCharsets.UTF_8)));

            var idSecretExpiredAt = decoded.split(TOKEN_PARTS_SEPARATOR);

            if (idSecretExpiredAt.length != 3) {
                throw InvalidAuthTokenException.invalidToken();
            }

            if (!idSecretExpiredAt[1].equals(SECRET)) {
                throw InvalidAuthTokenException.invalidToken();
            }

            var expiredAt = Instant.parse(idSecretExpiredAt[2]);
            if (clock.instant().isAfter(expiredAt)) {
                throw InvalidAuthTokenException.expiredToken();
            }

            var userId = UUID.fromString(idSecretExpiredAt[0]);
            return new AuthenticationResult(new AuthenticatedUser(userId), expiredAt);
        } catch (Exception e) {
            if (e instanceof InvalidAuthTokenException) {
                throw e;
            }
            throw InvalidAuthTokenException.invalidToken();
        }
    }

    @Override
    public AuthToken refresh(String token) {
        var authenticated = validateToken(token);
        return ofUser(authenticated.user().id());
    }

    @Override
    public AuthToken ofUser(UUID id) {
        var expiresAt = clock.instant().plus(AUTH_TOKEN_DURATION);
        var token = String.join(TOKEN_PARTS_SEPARATOR, id.toString(), SECRET, expiresAt.toString());
        return new AuthToken(Base64.getUrlEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8)), expiresAt);
    }

}
