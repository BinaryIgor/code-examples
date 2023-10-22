package com.binaryigor.apitests.support;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TestHttpClient {

    private final HttpClient client;
    private final Supplier<Integer> port;
    private final ObjectMapper objectMapper;

    public TestHttpClient(HttpClient client, Supplier<Integer> port,
                          ObjectMapper objectMapper) {
        this.client = client;
        this.port = port;
        this.objectMapper = objectMapper;
    }

    public TestHttpClient(Supplier<Integer> port, ObjectMapper objectMapper) {
        this(HttpClient.newHttpClient(), port, objectMapper);
    }

    public TestHttpResponse executeRequest(String path,
                                           String method,
                                           Map<String, List<String>> headers,
                                           byte[] body) {
        try {
            var requestBuilder = HttpRequest.newBuilder(uriFromPath(path))
                    .method(method, bodyPublisher(body));

            if (headers != null) {
                headers.forEach((k, vs) -> {
                    vs.forEach(v -> requestBuilder.header(k, v));
                });
            }

            var response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());

            return new TestHttpResponse(response.statusCode(), response.headers().map(), response.body(), objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest.BodyPublisher bodyPublisher(byte[] body) {
        return body == null || body.length == 0 ?
                HttpRequest.BodyPublishers.noBody() :
                HttpRequest.BodyPublishers.ofByteArray(body);
    }

    private URI uriFromPath(String path) throws Exception {
        var sanitizedPath = path.startsWith("/") ? path.replaceFirst("/", "") : path;
        return new URI("http://localhost:%d/%s".formatted(port.get(), sanitizedPath));
    }

    public RequestBuilder request() {
        return new RequestBuilder();
    }

    public class RequestBuilder {
        private final Map<String, List<String>> headers = new HashMap<>();
        private String path = "";
        private String method = "GET";
        private byte[] body;

        public RequestBuilder path(String path) {
            this.path = path;
            return this;
        }

        public RequestBuilder method(String method) {
            this.method = method;
            return this;
        }

        public RequestBuilder GET() {
            return method("GET");
        }

        public RequestBuilder POST() {
            return method("POST");
        }

        public RequestBuilder PUT() {
            return method("PUT");
        }

        public RequestBuilder header(String key, String value) {
            headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            return this;
        }

        public RequestBuilder body(byte[] body) {
            this.body = body;
            return this;
        }

        public RequestBuilder body(Object body) {
            try {
                this.body = objectMapper.writeValueAsBytes(body);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return header("content-type", "application/json");
        }

        public TestHttpResponse execute() {
            return executeRequest(path, method, headers, body);
        }
    }
}
