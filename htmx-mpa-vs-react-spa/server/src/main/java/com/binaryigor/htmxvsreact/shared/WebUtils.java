package com.binaryigor.htmxvsreact.shared;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class WebUtils {

    public static Optional<HttpServletRequest> currentRequest() {
        var ra = RequestContextHolder.getRequestAttributes();
        if (ra instanceof ServletRequestAttributes sra) {
            return Optional.of(sra.getRequest());
        }
        return Optional.empty();
    }

    public static boolean shouldRespondWithHTML() {
        return currentRequest()
            .map(r -> {
                var contentType = r.getHeader("content-type");
                if (contentType != null && contentType.contains("json")) {
                    return false;
                }
                var accept = r.getHeader("accept");
                return accept == null || !accept.contains("json");
            })
            .orElse(true);
    }
}
