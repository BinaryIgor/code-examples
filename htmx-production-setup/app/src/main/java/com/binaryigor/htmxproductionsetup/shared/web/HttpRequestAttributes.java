package com.binaryigor.htmxproductionsetup.shared.web;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class HttpRequestAttributes {

    public static final String USER_ATTRIBUTE = "com.binaryigor.user";
    public static final String REQUEST_LANGUAGE_ATTRIBUTE = "com.binaryigor.request.language";

    public static void set(String key, Object value) {
        var requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes sa) {
            sa.setAttribute(key, value, RequestAttributes.SCOPE_REQUEST);
        }
    }

    public static <T> Optional<T> get(String key, Class<T> type) {
        var requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes sa) {
            return Optional.ofNullable(type.cast(sa.getRequest().getAttribute(key)));
        }
        return Optional.empty();
    }
}
