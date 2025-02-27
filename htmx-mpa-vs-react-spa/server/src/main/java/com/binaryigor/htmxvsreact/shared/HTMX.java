package com.binaryigor.htmxvsreact.shared;

import jakarta.servlet.http.HttpServletRequest;

public class HTMX {

    public static String REQUEST_HEADER = "hx-request";
    public static String REDIRECT_HEADER = "hx-redirect";

    public static boolean isHTMXRequest() {
        var currentRequest = WebUtils.currentRequest();
        return currentRequest.isPresent() && isHTMXRequest(currentRequest.get());
    }

    public static boolean isHTMXRequest(HttpServletRequest request) {
        return request.getHeader(REQUEST_HEADER) != null;
    }
}
