package com.binaryigor.htmxproductionsetup.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.binaryigor.htmxproductionsetup.shared.AuthToken;
import com.binaryigor.htmxproductionsetup.shared.AuthenticatedUser;
import com.binaryigor.htmxproductionsetup.shared.PropertiesConverter;
import com.binaryigor.htmxproductionsetup.shared.exception.InvalidAuthTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class JWTAuthTokens implements AuthTokenCreator, AuthTokenAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthTokens.class);
    private final Clock clock;
    private final String issuer;
    private final Algorithm algorithm;
    private final Duration tokenDuration;

    public JWTAuthTokens(Clock clock, String issuer, Algorithm algorithm, Duration tokenDuration) {
        this.clock = clock;
        this.issuer = issuer;
        this.algorithm = algorithm;
        this.tokenDuration = tokenDuration;
    }

    /*
    https://crypto.stackexchange.com/questions/53826/hmac-sha256-vs-hmac-sha512-for-jwt-api-authentication
    Both algorithms provide plenty of security, near the output size of the hash.
    So even though HMAC-512 will be stronger, the difference is inconsequential.
    If this ever breaks it is because the algorithm itself is broken and as both hash algorithms are related, it is likely that both would be in trouble.
    However, no such attack is known and the HMAC construct itself appears to be very strong indeed.

    SHA-512 is indeed faster than SHA-256 on 64 bit machines.
    It may be that the overhead provided by the block size of SHA-512 is detrimental to HMAC-ing short length message sizes. But you can speedup larger messages sizes using HMAC-SHA-512 for sure.
    Then again, SHA-256 is plenty fast itself, and is faster on 32 bit and lower machines, so I'd go for HMAC-SHA-256 if lower end machines could be involved.

    Note that newer x86 processors also contain SHA-1 and SHA-256 accelerator hardware, so that may shift the speed advantage back into SHA-256's favor compared to SHA-512.
    */
    @Autowired
    public JWTAuthTokens(Clock clock, AuthConfig config) {
        this(clock, config.issuer(),
                Algorithm.HMAC512(PropertiesConverter.bytesFromString(config.tokenKey())),
                config.tokenDuration());
    }

    private String newToken(String issuer,
                            UUID subject,
                            Instant issuedAt,
                            Instant expiresAt,
                            Algorithm algorithm) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(subject.toString())
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .sign(algorithm);
    }

    @Override
    public AuthenticationResult authenticate(String token) {
        return validateToken(token);
    }

    private AuthenticationResult validateToken(String token) {
        UUID userId;
        Instant expiresAt;

        try {
            var decodedToken = tokenVerifier().verify(token);
            userId = UUID.fromString(decodedToken.getSubject());
            expiresAt = decodedToken.getExpiresAtAsInstant();
        } catch (Exception e) {
            logger.warn("Invalid token", e);
            throw InvalidAuthTokenException.invalidToken();
        }

        // If needed, get user + their additional data from the database
        return new AuthenticationResult(new AuthenticatedUser(userId), expiresAt);
    }

    private JWTVerifier tokenVerifier() {
        var builder = JWT.require(algorithm)
                .withIssuer(issuer);

        // for tests purposes
        if (builder instanceof JWTVerifier.BaseVerification b) {
            return b.build(clock);
        }

        return builder.build();
    }

    @Override
    public AuthToken refresh(String token) {
        var result = validateToken(token);
        return ofUser(result.user().id());
    }

    @Override
    public AuthToken ofUser(UUID id) {
        return token(id, clock.instant());
    }

    private AuthToken token(UUID id, Instant issuedAt) {
        var expiresAt = issuedAt.plus(tokenDuration);
        var token = newToken(issuer, id, issuedAt, expiresAt, algorithm);
        return new AuthToken(token, expiresAt);
    }
}
