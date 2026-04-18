package com.binaryigor.complexity_alternative.app;

import jakarta.servlet.http.HttpServletRequest;

public class HTMX {

    public static String REQUEST_HEADER = "hx-request";

    public static boolean isHTMXRequest() {
        var currentRequest = WebTools.currentRequest();
        return currentRequest.isPresent() && isHTMXRequest(currentRequest.get());
    }

    public static boolean isHTMXRequest(HttpServletRequest request) {
        return request.getHeader(REQUEST_HEADER) != null;
    }
}
