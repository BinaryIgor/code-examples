package com.binaryigor.httpserver.utils;

import com.binaryigor.httpserver.server.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class HttpResponses {


    public static HttpResponse text(int code, String text) {
        var textBytes = text.getBytes(StandardCharsets.UTF_8);
        return new HttpResponse(code,
                Map.of(HttpHeaders.CONTENT_TYPE, List.of("text/plain"),
                        HttpHeaders.CONTENT_LENGTH, List.of(String.valueOf(textBytes.length))),
                textBytes);
    }

    public static HttpResponse html(int code, String html) {
        return html(code, html, "utf-8");
    }

    public static HttpResponse html(int code, String html, String charset) {
        var htmlBytes = html.getBytes(StandardCharsets.UTF_8);
        return new HttpResponse(code,
                Map.of(HttpHeaders.CONTENT_TYPE, List.of("text/html; charset=%s".formatted(charset)),
                        HttpHeaders.CONTENT_LENGTH, List.of(String.valueOf(htmlBytes.length))),
                htmlBytes);
    }

    public static HttpResponse redirect(String location) {
        return new HttpResponse(303, Map.of("location", List.of(location)), null);
    }
}
