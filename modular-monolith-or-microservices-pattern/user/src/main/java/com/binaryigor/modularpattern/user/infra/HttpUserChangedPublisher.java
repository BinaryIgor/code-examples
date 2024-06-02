package com.binaryigor.modularpattern.user.infra;

import com.binaryigor.modularpattern.shared.contracts.UserChangedEvent;
import com.binaryigor.modularpattern.shared.events.AppEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpUserChangedPublisher {

    private final Logger logger = LoggerFactory.getLogger(HttpUserChangedPublisher.class);
    private final HttpClient httpClient;
    private final String host;
    private final Duration publishTimeout;
    private final ObjectMapper objectMapper;

    public HttpUserChangedPublisher(HttpClient httpClient,
                                    String host,
                                    Duration publishTimeout,
                                    ObjectMapper objectMapper,
                                    AppEvents appEvents) {
        this.httpClient = httpClient;
        this.host = host;
        this.publishTimeout = publishTimeout;
        this.objectMapper = objectMapper;

        appEvents.subscribe(UserChangedEvent.class, this::publish);
    }

    private void publish(UserChangedEvent event) {
        try {
            var request = HttpRequest.newBuilder()
                .uri(new URI(host + "/events/" + event.getClass().getSimpleName()))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(event)))
                .header("content-type", "application/json")
                .timeout(publishTimeout)
                .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Non-200 http status: " + response.statusCode());
            }
        } catch (Exception e) {
            logger.error("Failure while publishing UserChangedEvent to {} host...", host, e);
            throw new RuntimeException(e);
        }
    }
}
