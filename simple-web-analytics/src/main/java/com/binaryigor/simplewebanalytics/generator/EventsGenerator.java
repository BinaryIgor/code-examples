package com.binaryigor.simplewebanalytics.generator;

import com.binaryigor.simplewebanalytics.web.AnalyticsEventRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

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
import java.util.stream.Stream;

@Profile("events-generator")
@Component
@RequestMapping
public class EventsGenerator {

    private static final Random RANDOM = new SecureRandom();
    private static final long MAX_EVENT_SECONDS_IN_THE_PAST = TimeUnit.DAYS.toSeconds(31);
    private static final List<String> IPS = Stream.of(
            "58.154.154",
            "241.39.40",
            "137.235.26",
            "92.168.152",
            "141.209.126",
            "130.12.56",
            "81.100.144",
            "163.92.154",
            "201.223.48",
            "62.200.216",
            "36.221.145",
            "187.138.229",
            "172.252.118"
        ).flatMap(ipPrefix -> Stream.generate(() -> {
            var lastOctet = RANDOM.nextInt(255);
            return ipPrefix + "." + lastOctet;
        }).limit(25))
        .toList();
    private static final List<UUID> DEVICE_IDS = Stream.generate(UUID::randomUUID).limit(1000).toList();
    private static final List<UUID> USER_IDS = Stream.generate(UUID::randomUUID).limit(500).toList();
    private static final List<String> APP_PATHS = List.of("home", "account", "profile", "search");
    private static final List<String> BROWSERS = List.of("Chrome", "Firefox", "Safari", "Edge", "Opera", "Unknown");
    private static final List<String> OSES = List.of("Linux", "Windows", "Mac OS", "Android", "iOS", "Unknown");
    private static final List<String> DEVICES = List.of("Desktop", "Tablet", "Mobile", "Unknown");
    private static final List<String> EVENT_TYPES = List.of("home-view", "account-view", "profile-view", "search-view", "profile-edit", "search-input");
    private static final List<String> SEARCH_INPUT_EVENT_INPUTS = List.of("BTC", "Bitcoin", "Gold", "Gold vs BTC", "Maybe Silver", "Inflation facts", "LLMs vs Humans", "Is AI really that powerful");
    private static final Logger logger = LoggerFactory.getLogger(EventsGenerator.class);
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final String serverUrl;
    private final HttpClient httpClient;

    public EventsGenerator(ObjectMapper objectMapper,
                           @Value("${events-generator.server-url}")
                           String serverUrl,
                           Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.serverUrl = serverUrl;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    public void generate(int size, int concurrency) {
        var start = clock.instant();
        logger.info("Inserting {} events...", size);

        var sizePart = size > 1000 ? size / 100 : size / 10;

        var semaphore = new Semaphore(concurrency);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var insertsCounter = new AtomicInteger(0);

            IntStream.range(0, size)
                .forEach(i -> {
                    var eventTimestamp = randomEventTimestamp();
                    var userId = randomUserIdOrNull();
                    var event = randomAnalyticsEventRequest();
                    sendEventOnExecutorWithRandomDelay(executor, semaphore, event, eventTimestamp, userId,
                        i < concurrency,
                        () -> {
                            var inserted = insertsCounter.incrementAndGet();
                            if (inserted > 0 && inserted % sizePart == 0) {
                                logger.info("{}/{} events were inserted...", inserted, size);
                            }
                        });

                });
        }

        var end = clock.instant();
        logger.info("{} events inserted! It took: {}", size, Duration.between(start, end));
    }

    private void sendEventOnExecutorWithRandomDelay(Executor executor,
                                                    Semaphore semaphore,
                                                    AnalyticsEventRequest eventRequest,
                                                    Instant eventTimestamp,
                                                    UUID userId,
                                                    boolean warmup,
                                                    Runnable onSend) {
        try {
            semaphore.acquire();
            executor.execute(() -> {
                try {
                    if (warmup) {
                        var delay = RANDOM.nextInt(3000);
                        Thread.sleep(delay);
                    }
                    sendEvent(eventRequest, eventTimestamp, userId);
                    onSend.run();
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

    private void sendEvent(AnalyticsEventRequest eventRequest,
                           Instant eventTimestamp,
                           UUID userId) {
        try {
            var body = objectMapper.writeValueAsString(eventRequest);
            var requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("content-type", "application/json")
                .header("timestamp", eventTimestamp.toString())
                .header("real-ip", oneOf(IPS));

            if (userId != null) {
                requestBuilder.header("user-id", userId.toString());
            }

            var request = requestBuilder.timeout(Duration.ofSeconds(5)).build();

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
        return new AnalyticsEventRequest(oneOf(DEVICE_IDS),
            "https://some.app/" + oneOf(APP_PATHS),
            oneOf(BROWSERS),
            oneOf(OSES),
            oneOf(DEVICES),
            eventTypeWithData.type,
            eventTypeWithData.data);
    }

    private UUID randomUserIdOrNull() {
        if (RANDOM.nextBoolean()) {
            return oneOf(USER_IDS);
        }
        return null;
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
