package com.binaryigor.smart.gateway;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(Routes.class)
public class SmartGatewayConfiguration {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        var httpClient = HttpClient.newHttpClient();

        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        return builder.requestFactory(requestFactory).build();
    }
}
