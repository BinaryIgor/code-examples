package com.binaryigor.single.app;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class SecurityFilter implements Filter {

    // Keep in sync with LoadTest!
    private static final String SECRET_QUERY_STRING = "17e57c8c-60ea-4b4a-8d48-5967f03b942c";
    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (isLocalhost(request.getRemoteAddr())) {
            log.info("Localhost request, skipping security check!");
            chain.doFilter(request, response);
            return;
        }

        var httpRequest = (HttpServletRequest) request;

        var authorized = Optional.ofNullable(httpRequest.getQueryString())
                .map(q -> q.contains(SECRET_QUERY_STRING))
                .orElse(false);

        if (authorized) {
            chain.doFilter(request, response);
        } else {
            log.warn("Somebody tried to poke around! Their request:");
            log.warn("Method: {}, url: {}, query: {}", httpRequest.getMethod(), httpRequest.getRequestURI(), httpRequest.getQueryString());
            var httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(404);
            httpResponse.getWriter().write("Don't know anything about it");
        }
    }

    private boolean isLocalhost(String clientIp) {
        return clientIp.startsWith("localhost") || clientIp.startsWith("0.0.0.0") ||
                clientIp.startsWith("127.0.0.1") || clientIp.startsWith("::1");
    }
}
