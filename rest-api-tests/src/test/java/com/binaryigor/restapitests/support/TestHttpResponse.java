package com.binaryigor.restapitests.support;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public record TestHttpResponse(int statusCode,
                               Map<String, List<String>> headers,
                               byte[] body,
                               ObjectMapper objectMapper) {

    public <T> T bodyAsJson(Class<T> clazz) {
        try {
            return objectMapper.readValue(body, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
