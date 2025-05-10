package com.binaryigor.httpserver.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record HttpRequest(String method,
                          String url,
                          Map<String, List<String>> headers,
                          byte[] body,
                          File bodyFile) {

    public HttpRequest(String method, String url, Map<String, List<String>> headers) {
        this(method, url, headers, null, null);
    }

    public HttpRequest(String method,
                       String url,
                       Map<String, List<String>> headers,
                       byte[] body) {
        this(method, url, headers, body, null);
    }

    public HttpRequest(String method,
                       String url,
                       Map<String, List<String>> headers,
                       File bodyFile) {
        this(method, url, headers, null, bodyFile);
    }

    public Optional<String> header(String name) {
        var headersOfName = headers.getOrDefault(name.toLowerCase(), List.of());
        return headersOfName.isEmpty() ? Optional.empty() : Optional.of(headersOfName.getFirst());
    }

    public boolean hasBody() {
        return (body != null && body.length > 0) || bodyFile != null;
    }

    public boolean hasBodyInFile() {
        return bodyFile != null;
    }

    public InputStream bodyAsStream() {
        if (!hasBody()) {
            throw new RuntimeException("Request doesn't have body");
        }
        try {
            if (hasBodyInFile()) {
                return new FileInputStream(bodyFile);
            }
            return new ByteArrayInputStream(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to turn request body into bytes stream", e);
        }
    }
}
