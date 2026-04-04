package com.binaryigor.complexity_alternative.app;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "web")
public record WebProperties(String cssPath, String htmxPath, List<String> componentPaths) {
}
