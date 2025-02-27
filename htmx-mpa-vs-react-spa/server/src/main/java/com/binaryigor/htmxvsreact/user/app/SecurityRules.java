package com.binaryigor.htmxvsreact.user.app;

import com.binaryigor.htmxvsreact.user.domain.AuthenticatedUser;
import com.binaryigor.htmxvsreact.user.domain.exception.UnauthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.Optional;
import java.util.function.Predicate;

public class SecurityRules {

    private final Predicates predicates;
    private final String unauthorizedRedirect;

    public SecurityRules(Predicates predicates, String unauthorizedRedirect) {
        this.predicates = predicates;
        this.unauthorizedRedirect = unauthorizedRedirect;
    }

    @Autowired
    public SecurityRules() {
        this(new Predicates(
                e -> e.method() == HttpMethod.OPTIONS ||
                     e.url().contains(".css") ||
                     e.url().contains(".js") ||
                     e.url().contains("/sign-in") ||
                     e.url().contains("/user-info") ||
                     e.url().contains("/swagger-ui/")),
            "/sign-in");
    }

    public void validateAccess(SecurityEndpoint endpoint,
                               Optional<AuthenticatedUser> user) {
        if (predicates.publicEndpoint.test(endpoint)) {
            return;
        }
        if (!user.isPresent()) {
            throw new UnauthenticatedException();
        }
    }

    public String unauthorizedRedirect() {
        return unauthorizedRedirect;
    }

    public record Predicates(Predicate<SecurityEndpoint> publicEndpoint) {
    }

}
