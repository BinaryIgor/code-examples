package com.binaryigor.simplewebanalytics.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@RestController
@RequestMapping
public class InternalController {

    private static final long MAX_EVENT_SECONDS_IN_THE_PAST = TimeUnit.DAYS.toSeconds(31);
    private static final List<String> APP_PATHS = List.of("home", "account", "profile", "search");
    private static final List<String> BROWSERS = List.of("Chrome", "Firefox", "Safari", "Edge", "Opera", "Unknown");
    private static final List<String> OSES = List.of("Linux", "Windows", "Mac OS", "Android", "iOS", "Unknown");
    private static final List<String> DEVICES = List.of("Desktop", "Tablet", "Mobile", "Unknown");
    private static final List<String> EVENT_TYPES = List.of("home-view", "account-view", "profile-view", "search-view", "profile-edit", "search-input");
    private static final List<String> SEARCH_INPUT_EVENT_INPUTS = List.of("BTC", "Bitcoin", "Gold", "Gold vs BTC", "Maybe Silver", "Inflation facts", "LLMs vs Humans", "Is AI really that powerful");
    private static final Logger logger = LoggerFactory.getLogger(InternalController.class);
    private static final Random RANDOM = new SecureRandom();
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final int serverPort;
    private final HttpClient httpClient;

    public InternalController(ObjectMapper objectMapper,
                              Clock clock,
                              @Value("${server.port}")
                              int serverPort) {
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.serverPort = serverPort;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    @PostMapping("/internal/send-random-analytics-events")
    void generateAnalyticsEvents(@RequestParam(value = "size", required = false, defaultValue = "10000") int size,
                                 @RequestParam(value = "concurrency", required = false, defaultValue = "100") int concurrency) {
        var start = Instant.now();
        logger.info("Inserting {} events...", size);

        var sizePart = size > 1000 ? size / 100 : size / 10;

        var semaphore = new Semaphore(concurrency);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var insertsCounter = new AtomicInteger(0);

            IntStream.range(0, size)
                .forEach(i -> {
                    var eventTimestamp = randomEventTimestamp();
                    var event = randomAnalyticsEventRequest();
                    sendEventOnExecutorWithRandomDelay(executor, semaphore, event, eventTimestamp, insertsCounter,
                        i < concurrency);

                    var inserted = insertsCounter.get();
                    if (inserted > 0 && inserted % sizePart == 0) {
                        logger.info("{}/{} events were inserted...", inserted, size);
                    }
                });
        }

        var end = Instant.now();
        logger.info("{} events inserted! It took: {}", size, Duration.between(start, end));
    }

    private void sendEventOnExecutorWithRandomDelay(Executor executor,
                                                    Semaphore semaphore,
                                                    AnalyticsEventRequest eventRequest,
                                                    Instant eventTimestamp,
                                                    AtomicInteger insertsCounter,
                                                    boolean warmup) {
        try {
            semaphore.acquire();
            executor.execute(() -> {
                try {
                    if (warmup) {
                        var delay = RANDOM.nextInt(1000);
                        Thread.sleep(delay);
                    }
                    sendEvent(eventRequest, eventTimestamp);
                    insertsCounter.incrementAndGet();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    semaphore.release();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendEvent(AnalyticsEventRequest eventRequest, Instant eventTimestamp) {
        try {
            var body = objectMapper.writeValueAsString(eventRequest);
            var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:%d/analytics/events".formatted(serverPort)))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("content-type", "application/json")
                .header("timestamp", eventTimestamp.toString())
                .timeout(Duration.ofSeconds(5))
                .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (!HttpStatus.valueOf(response.statusCode()).is2xxSuccessful()) {
                logger.warn("non-200 response: {}", response);
            }
        } catch (Exception e) {
            logger.error("Failed analytics event request: ", e);
        }
    }

    private Instant randomEventTimestamp() {
        var now = clock.instant();
        var secondsInThePast = RANDOM.nextLong(MAX_EVENT_SECONDS_IN_THE_PAST);
        return now.minusSeconds(secondsInThePast);
    }

    private AnalyticsEventRequest randomAnalyticsEventRequest() {
        var eventTypeWithData = randomEventTypeWithData();
        return new AnalyticsEventRequest(UUID.randomUUID(),
            "https://some.app/" + oneOf(APP_PATHS),
            oneOf(BROWSERS),
            oneOf(OSES),
            oneOf(DEVICES),
            eventTypeWithData.type,
            eventTypeWithData.data);
    }

    private <T> T oneOf(List<T> choices) {
        var idx = RANDOM.nextInt(choices.size());
        return choices.get(idx);
    }

    private EventTypeWithData randomEventTypeWithData() {
        var type = oneOf(EVENT_TYPES);

        Object data;
        if (type.contains("search-input")) {
            data = Map.of("input", oneOf(SEARCH_INPUT_EVENT_INPUTS));
        } else if (type.contains("profile-view") || type.contains("profile-edit")) {
            data = Map.of("userId", UUID.randomUUID());
        } else {
            data = null;
        }

        return new EventTypeWithData(type, data);
    }

    private record EventTypeWithData(String type, Object data) {
    }
}
