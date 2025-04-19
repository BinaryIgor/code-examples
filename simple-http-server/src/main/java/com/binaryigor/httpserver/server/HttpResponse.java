package com.binaryigor.httpserver.server;

import java.util.List;
import java.util.Map;

public record HttpResponse(int responseCode,
                           Map<String, List<String>> headers,
                           byte[] body) {
}
