package com.binaryigor.htmxvsreact.user.infra.web;

import jakarta.servlet.http.Cookie;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class Cookies {

    public static final String TOKEN_KEY = "token";

    private final Clock clock;
    private final String sameSite;

    public Cookies(Clock clock, String sameSite) {
        this.clock = clock;
        this.sameSite = sameSite;
    }

    public Cookie token(String token, int maxAge) {
        var cookie = new Cookie(TOKEN_KEY, token);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", sameSite);
        return cookie;
    }

    public Cookie token(String token, Instant tokenExpiresAt) {
        var maxAge = Duration.between(clock.instant(), tokenExpiresAt);
        return token(token, (int) maxAge.getSeconds());
    }

    public Cookie expiredToken() {
        return token("expired", 0);
    }

    public Optional<String> tokenValue(Cookie[] cookies) {
        return Optional.ofNullable(cookies).map(cs -> {
            for (var c : cs) {
                if (c.getName().equals(TOKEN_KEY)) {
                    return c.getValue();
                }
            }
            return null;
        });
    }
}
