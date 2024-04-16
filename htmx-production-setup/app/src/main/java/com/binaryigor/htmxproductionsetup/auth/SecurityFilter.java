package com.binaryigor.htmxproductionsetup.auth;

import com.binaryigor.htmxproductionsetup.shared.exception.AccessForbiddenException;
import com.binaryigor.htmxproductionsetup.shared.web.Cookies;
import com.binaryigor.htmxproductionsetup.shared.exception.InvalidAuthTokenException;
import com.binaryigor.htmxproductionsetup.shared.exception.UnauthenticatedException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

@Component
public class SecurityFilter implements Filter {

    static final String REAL_IP_HEADER = "x-real-ip";
    private static final String REDIRECT_ON_FAILED_AUTH_PAGE = "/sign-in";

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    private final AuthTokenAuthenticator authTokenAuthenticator;
    private final SecurityRules securityRules;
    private final Cookies cookies;
    private final Clock clock;
    private final Duration issueNewTokenBeforeExpirationDuration;

    public SecurityFilter(AuthTokenAuthenticator authTokenAuthenticator,
                          SecurityRules securityRules,
                          Cookies cookies,
                          Clock clock,
                          @Value("${auth.issue-new-token-before-expiration-duration}")
                          Duration issueNewTokenBeforeExpirationDuration) {
        this.authTokenAuthenticator = authTokenAuthenticator;
        this.securityRules = securityRules;
        this.cookies = cookies;
        this.clock = clock;
        this.issueNewTokenBeforeExpirationDuration = issueNewTokenBeforeExpirationDuration;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        var request = (HttpServletRequest) servletRequest;
        var response = (HttpServletResponse) servletResponse;

        var endpoint = new SecurityEndpoint(request.getRequestURI(),
                HttpMethod.valueOf(request.getMethod()));

        try {
            var token = cookies.tokenValue(request.getCookies());

            var authResult = token.map(authTokenAuthenticator::authenticate);
            authResult.ifPresent(r ->
                    AuthenticatedUserRequestHolder.set(r.user()));
            logger.info("Auth result: {}", authResult);

            securityRules.validateAccess(endpoint,
                    isAllowedPrivateClientRequest(request),
                    authResult.map(AuthenticationResult::user));

            authResult.ifPresent(r -> {
                if (shouldIssueNewToken(r)) {
                    issueNewToken(response, token.get());
                }
            });

            chain.doFilter(servletRequest, servletResponse);
        } catch (UnauthenticatedException | InvalidAuthTokenException e) {
            sendExceptionResponse(request, response, 401, e);
        } catch (AccessForbiddenException e) {
            sendExceptionResponse(request, response, 403, e);
        }
    }

    private boolean isAllowedPrivateClientRequest(HttpServletRequest request) {
        var clientIp = Optional.ofNullable(request.getHeader(REAL_IP_HEADER))
                .orElseGet(request::getRemoteAddr);

        logger.info("Client ip: {}", clientIp);

        return isLocalhost(clientIp);
    }

    private boolean isLocalhost(String clientIp) {
        return clientIp.equals("localhost") || clientIp.equals("0.0.0.0") || clientIp.equals("127.0.0.1") ||
               clientIp.equals("0:0:0:0:0:0:0:1") || clientIp.equals("::1");
    }

    private boolean shouldIssueNewToken(AuthenticationResult result) {
        return Duration.between(clock.instant(), result.expiresAt())
                       .compareTo(issueNewTokenBeforeExpirationDuration) <= 0;
    }

    private void issueNewToken(HttpServletResponse response, String currentToken) {
        var authToken = authTokenAuthenticator.refresh(currentToken);
        response.addCookie(cookies.token(authToken.value(), authToken.expiresAt()));
    }

    //TODO: sth better with exception?
    private void sendExceptionResponse(HttpServletRequest request,
                                       HttpServletResponse response,
                                       int status,
                                       Throwable exception) {
        logger.warn("Sending redirect from {} status to {}: {} request", status, request.getMethod(), request.getRequestURI());
        logger.warn("Problem:", exception);
        try {
            response.setStatus(302);
            response.setHeader("Location", REDIRECT_ON_FAILED_AUTH_PAGE);
            // TODO: maybe only if exists
            response.addCookie(cookies.expiredToken());
        } catch (Exception e) {
            logger.error("Problem while writing response body to HttpServletResponse", e);
        }
    }
}
