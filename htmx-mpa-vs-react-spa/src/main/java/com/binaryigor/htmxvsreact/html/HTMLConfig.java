package com.binaryigor.htmxvsreact.html;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("html")
public record HTMLConfig(String cssPath, String htmxPath) {
}
