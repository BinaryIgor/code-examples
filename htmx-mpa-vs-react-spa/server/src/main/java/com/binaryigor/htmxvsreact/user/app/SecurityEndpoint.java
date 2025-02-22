package com.binaryigor.htmxvsreact.user.app;

import org.springframework.http.HttpMethod;

public record SecurityEndpoint(String url, HttpMethod method) {

}
