package com.binaryigor.htmxproductionsetup.auth;

import org.springframework.http.HttpMethod;

public record SecurityEndpoint(String url, HttpMethod method) {
}
