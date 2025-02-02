package com.binaryigor.htmxvsreact.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class HTMX {

    public static String REQUEST_HEADER = "hx-request";
    public static String REDIRECT_HEADER = "hx-redirect";
    public static String TRIGGER_HEADER = "hx-trigger";

    private static Optional<HttpServletRequest> currentRequest() {
        var ra = RequestContextHolder.getRequestAttributes();
        if (ra instanceof ServletRequestAttributes sra) {
            return Optional.of(sra.getRequest());
        }
        return Optional.empty();
    }

    public static boolean isHTMXRequest() {
        var currentRequest = currentRequest();
        return currentRequest.isPresent() && isHTMXRequest(currentRequest.get());
    }

    public static boolean isHTMXRequest(HttpServletRequest request) {
        return request.getHeader(REQUEST_HEADER) != null;
    }

    public static void addClientReplaceUrlHeader(HttpServletResponse response, String url) {
        response.addHeader("hx-replace-url", url);
    }

    public static void addTriggerHeader(HttpServletResponse response, String trigger) {
        response.addHeader("hx-trigger", trigger);
    }
}
