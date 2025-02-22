package com.binaryigor.htmxvsreact.user.app;

import com.binaryigor.htmxvsreact.shared.WebUtils;
import com.binaryigor.htmxvsreact.shared.error.WebExceptionHandler;
import com.binaryigor.htmxvsreact.user.domain.AuthTokenAuthenticator;
import com.binaryigor.htmxvsreact.user.domain.AuthenticationResult;
import com.binaryigor.htmxvsreact.user.domain.exception.AccessForbiddenException;
import com.binaryigor.htmxvsreact.user.domain.exception.InvalidAuthTokenException;
import com.binaryigor.htmxvsreact.user.domain.exception.UnauthenticatedException;
import com.binaryigor.htmxvsreact.user.infra.web.Cookies;
import com.binaryigor.htmxvsreact.user.infra.web.CurrentRequestUser;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Duration;

public class SecurityFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);
    private final AuthTokenAuthenticator authTokenAuthenticator;
    private final SecurityRules securityRules;
    private final Cookies cookies;
    private final WebExceptionHandler webExceptionHandler;
    private final Clock clock;
    private final Duration issueNewTokenBeforeExpirationDuration;

    public SecurityFilter(AuthTokenAuthenticator authTokenAuthenticator,
                          SecurityRules securityRules,
                          Cookies cookies,
                          WebExceptionHandler webExceptionHandler,
                          Clock clock,
                          Duration issueNewTokenBeforeExpirationDuration) {
        this.authTokenAuthenticator = authTokenAuthenticator;
        this.securityRules = securityRules;
        this.cookies = cookies;
        this.clock = clock;
        this.webExceptionHandler = webExceptionHandler;
        this.issueNewTokenBeforeExpirationDuration = issueNewTokenBeforeExpirationDuration;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) {
        var request = (HttpServletRequest) servletRequest;
        var response = (HttpServletResponse) servletResponse;

        var endpoint = new SecurityEndpoint(request.getRequestURI(),
            HttpMethod.valueOf(request.getMethod()));

        try {
            var token = cookies.tokenValue(request.getCookies());

            var authResult = token.map(authTokenAuthenticator::authenticate);
            authResult.ifPresent(r -> CurrentRequestUser.set(r.user()));

            securityRules.validateAccess(endpoint, authResult.map(AuthenticationResult::user));

            authResult.ifPresent(r -> {
                if (shouldIssueNewToken(r)) {
                    issueNewToken(response, token.get());
                }
            });

            chain.doFilter(servletRequest, servletResponse);
        } catch (UnauthenticatedException | InvalidAuthTokenException e) {
            sendExceptionResponse(request, response, HttpStatus.UNAUTHORIZED, e, true);
        } catch (AccessForbiddenException e) {
            sendExceptionResponse(request, response, HttpStatus.FORBIDDEN, e, false);
        } catch (Exception e) {
            logger.error("Unknown exception while handling security...", e);
            sendExceptionResponse(request, response, HttpStatus.INTERNAL_SERVER_ERROR,
                new IllegalStateException("Unknown Error"), false);
        }
    }

    private boolean shouldIssueNewToken(AuthenticationResult result) {
        return Duration.between(clock.instant(), result.expiresAt())
                   .compareTo(issueNewTokenBeforeExpirationDuration) <= 0;
    }

    private void issueNewToken(HttpServletResponse response, String currentToken) {
        var authToken = authTokenAuthenticator.refresh(currentToken);
        response.addCookie(cookies.token(authToken.value(), authToken.expiresAt()));
    }

    private void sendExceptionResponse(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HttpStatus status,
                                       Throwable exception,
                                       boolean forceSignOut) {
        try {
            if (forceSignOut) {
                response.addCookie(cookies.expiredToken());
            }
            if (status == HttpStatus.UNAUTHORIZED && WebUtils.shouldRespondWithHTML()) {
                response.sendRedirect(securityRules.unauthorizedRedirect());
            } else {
                webExceptionHandler.handle(response, status, exception);
            }
        } catch (Exception e) {
            logger.error("Problem while writing response body to HttpServletResponse", e);
        }
    }
}
