package com.binaryigor.htmxproductionsetup.shared.web;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
public class Cookies {

    public static final String TOKEN_KEY = "token";

    private final Clock clock;

    public Cookies(Clock clock) {
        this.clock = clock;
    }

    public Cookie token(String token, int maxAge) {
        var cookie = new Cookie(TOKEN_KEY, token);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "Strict");
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
