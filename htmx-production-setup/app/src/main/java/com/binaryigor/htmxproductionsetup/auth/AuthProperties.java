package com.binaryigor.htmxproductionsetup.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(String issuer, String tokenKey, Duration tokenDuration) { }