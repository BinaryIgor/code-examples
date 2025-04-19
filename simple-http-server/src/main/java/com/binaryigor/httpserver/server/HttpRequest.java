package com.binaryigor.httpserver.server;

import java.util.List;
import java.util.Map;

public record HttpRequest(String method,
                          String url,
                          Map<String, List<String>> headers,
                          byte[] body) {

}
