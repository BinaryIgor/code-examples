package com.binaryigor.httpserver.server;

public interface HttpRequestHandler {
    HttpResponse handle(HttpRequest request);
}
