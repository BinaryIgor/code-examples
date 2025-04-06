package com.binaryigor.httpserver.server;

public interface HttpServer {

    void start(HttpRequestHandler handler);

    void stop();
}
