package com.binaryigor.modularpattern.project.infra;

import com.binaryigor.modularpattern.shared.contracts.UserClient;
import com.binaryigor.modularpattern.shared.contracts.UserView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Stream;

public class HttpUserClient implements UserClient {

    private final Logger logger = LoggerFactory.getLogger(HttpUserClient.class);
    private final HttpClient httpClient;
    private final String host;
    private final Duration readTimeout;
    private final ObjectMapper objectMapper;

    public HttpUserClient(HttpClient httpClient, String host, Duration readTimeout, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.host = host;
        this.readTimeout = readTimeout;
        this.objectMapper = objectMapper;
    }

    @Override
    public Stream<UserView> allUsers() {
        try {
            var request = HttpRequest.newBuilder()
                .uri(new URI(host + "/users"))
                .GET()
                .timeout(readTimeout)
                .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Non-200 http status: " + response.statusCode());
            }
            return response.body()
                .map(this::userFromJson);
        } catch (Exception e) {
            logger.error("Failure while getting users from {} host...", host, e);
            throw new RuntimeException(e);
        }
    }

    private UserView userFromJson(String json) {
        try {
            return objectMapper.readValue(json, UserView.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
