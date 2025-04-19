package com.binaryigor.httpserver.utils;

import com.binaryigor.httpserver.server.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class HttpResponses {

    public static HttpResponse html(int code, String html) {
        var htmlBytes = html.getBytes(StandardCharsets.UTF_8);
        return new HttpResponse(code,
                Map.of("content-type", List.of("text/html; charset=utf-8"),
                        "content-length", List.of(String.valueOf(htmlBytes.length))),
                htmlBytes);
    }

    public static HttpResponse redirect(String location) {
        return new HttpResponse(303, Map.of("location", List.of(location)), null);
    }
}
