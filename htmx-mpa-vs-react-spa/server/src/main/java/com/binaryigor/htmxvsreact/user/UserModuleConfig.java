package com.binaryigor.htmxvsreact.user;

import com.binaryigor.htmxvsreact.shared.error.WebExceptionHandler;
import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import com.binaryigor.htmxvsreact.shared.html.Translations;
import com.binaryigor.htmxvsreact.user.domain.*;
import com.binaryigor.htmxvsreact.user.infra.AuthProperties;
import com.binaryigor.htmxvsreact.user.infra.JWTAuthTokens;
import com.binaryigor.htmxvsreact.user.infra.BCryptPasswordHasher;
import com.binaryigor.htmxvsreact.user.app.SecurityFilter;
import com.binaryigor.htmxvsreact.user.app.SecurityRules;
import com.binaryigor.htmxvsreact.user.infra.SqlUserRepository;
import com.binaryigor.htmxvsreact.user.domain.exception.UserEmailValidationException;
import com.binaryigor.htmxvsreact.user.domain.exception.UserIncorrectPasswordException;
import com.binaryigor.htmxvsreact.user.domain.exception.UserPasswordValidationException;
import com.binaryigor.htmxvsreact.user.infra.web.Cookies;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Clock;

@Configuration
public class UserModuleConfig {

    @Bean
    InitializingBean userTranslationsInitializer(Translations translations) {
        return () -> {
            translations.register(UserEmailValidationException.class, (l, e) -> "Given email is not valid. It must contain '@' sign and a valid domain");
            translations.register(UserPasswordValidationException.class, (l, e) -> "Invalid password. It must have between 8 and 50 characters");
            translations.register(UserIncorrectPasswordException.class, (l, e) -> "Password is incorrect");
        };
    }

    @Bean
    JWTAuthTokens jwtAuthTokens(Clock clock, AuthProperties authProperties, UserRepository userRepository) {
        JWTAuthTokens.UserSource userSource = id -> userRepository.ofId(id).map(u -> new AuthenticatedUser(u.id(), u.language()));
        return new JWTAuthTokens(clock, authProperties, userSource);
    }

    @Bean
    SecurityRules securityRules() {
        return new SecurityRules();
    }

    @Bean
    SecurityFilter securityFilter(AuthTokenAuthenticator authTokenAuthenticator,
                                  SecurityRules securityRules,
                                  Cookies cookies,
                                  WebExceptionHandler webExceptionHandler,
                                  Clock clock,
                                  AuthProperties authProperties) {
        return new SecurityFilter(authTokenAuthenticator, securityRules, cookies, webExceptionHandler, clock,
            authProperties.issueNewTokenBeforeExpirationDuration());
    }

    @Bean
    Cookies cookies(Clock clock) {
        return new Cookies(clock, "Strict");
    }

    @Bean
    PasswordHasher passwordHasher() {
        return new BCryptPasswordHasher();
    }

    @Bean
    UserRepository userRepository(JdbcClient jdbcClient) {
        return new SqlUserRepository(jdbcClient);
    }

    @Bean
    UserService userService(UserRepository userRepository, PasswordHasher passwordHasher,
                            AuthTokenCreator authTokenCreator) {
        return new UserService(userRepository, passwordHasher, authTokenCreator);
    }

    @Bean
    UserClient userClient() {
        return new TheUserClient();
    }
}
