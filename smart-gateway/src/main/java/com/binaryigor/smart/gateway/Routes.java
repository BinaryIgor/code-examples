package com.binaryigor.smart.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("gateway")
public record Routes(List<Route> routes) {
}
