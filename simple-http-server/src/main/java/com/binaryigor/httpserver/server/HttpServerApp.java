package com.binaryigor.httpserver.server;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class HttpServerApp {

    public static void main(String[] args) {
        var server = new SimpleHttpServer(Executors.newVirtualThreadPerTaskExecutor(), 8080);

        server.start(r -> {
            var body = """
                    {
                        "id": 1,
                        "url": "%s"
                    }
                    """.formatted(r.url())
                    .getBytes(StandardCharsets.UTF_8);

            var headers = Map.of("Content-Type", List.of("application/json"),
                    "Content-Length", List.of(String.valueOf(body.length)));

            return new HttpResponse(200, headers, body);
        });
    }

}
