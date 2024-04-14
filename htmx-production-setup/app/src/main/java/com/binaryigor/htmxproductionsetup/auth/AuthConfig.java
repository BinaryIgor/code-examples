package com.binaryigor.htmxproductionsetup.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "auth")
public record AuthConfig(String issuer, String tokenKey, Duration tokenDuration) { }
