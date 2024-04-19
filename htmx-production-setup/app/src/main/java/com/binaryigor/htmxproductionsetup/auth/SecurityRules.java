package com.binaryigor.htmxproductionsetup.auth;

import com.binaryigor.htmxproductionsetup.shared.contracts.UserData;
import com.binaryigor.htmxproductionsetup.shared.exception.UnauthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Predicate;

@Component
public class SecurityRules {

    private final Predicates predicates;

    public SecurityRules(Predicates predicates) {
        this.predicates = predicates;
    }

    @Autowired
    public SecurityRules() {
        this(new Predicates(e ->
                e.url().contains("sign-in")
                || e.url().endsWith(".js")
                || e.url().endsWith(".css")
                || e.url().endsWith(".ico"),
                e -> e.url().contains("actuator")));
    }

    public void validateAccess(SecurityEndpoint endpoint,
                               boolean privateNetworkClient,
                               Optional<UserData> user) {
        if (predicates.publicEndpoint.test(endpoint)) {
            return;
        }

        if (privateNetworkClient && predicates.privateClientEndpoint.test(endpoint)) {
            return;
        }

        if (user.isEmpty()) {
            throw new UnauthenticatedException();
        }
    }

    public record Predicates(Predicate<SecurityEndpoint> publicEndpoint,
                             Predicate<SecurityEndpoint> privateClientEndpoint) {
    }

}
