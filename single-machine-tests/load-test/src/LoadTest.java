package src;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTest {

    static final Random RANDOM = new Random();
    static final AtomicLong REQUEST_ERRORS = new AtomicLong();

    public static void main(String[] args) throws Exception {
        var requests = envIntValueOrDefault("REQUESTS", 10_000);
        var ratePerSecond = envIntValueOrDefault("RATE_PER_SECOND", 100);
        var maxPendingFutures = envIntValueOrDefault("MAX_PENDING_FUTURES", 100);
        var connectTimeout = envIntValueOrDefault("CONNECT_TIMEOUT", 2_000);
        var requestTimeout = envIntValueOrDefault("REQUEST_TIMEOUT", 10_000);
        var endpoints = endpoints();

        System.out.println("About to make %d requests, with %d/s rate, %d and %d timeouts, to %s endpoints..."
                .formatted(requests, ratePerSecond, connectTimeout, requestTimeout, endpoints));

        var start = System.currentTimeMillis();

        var httpClient = newHttpClient(connectTimeout);

        var executor = Executors.newVirtualThreadPerTaskExecutor();

        var resultFutures = new LinkedList<Future<Long>>();
        var results = new LinkedList<Long>();

        for (var i = 0; i < requests; i++) {
            var result = executor.submit(() -> task(httpClient, requestTimeout, endpoints));
            resultFutures.add(result);

            var issuedRequests = i + 1;
            if (issuedRequests % ratePerSecond == 0 && issuedRequests < requests) {
                System.out.println("%s, %d/%d requests were issued, waiting 1s for the next packet..."
                        .formatted(LocalDateTime.now(), issuedRequests, requests));
                Thread.sleep(1000);
            }

            if (resultFutures.size() >= maxPendingFutures) {
                results.addAll(waitForResults(resultFutures));
                resultFutures.clear();
            }
        }

        if (!resultFutures.isEmpty()) {
            results.addAll(waitForResults(resultFutures));
            resultFutures.clear();
        }

        var sortedResults = results.stream().sorted().toList();

        var duration = Duration.ofMillis(System.currentTimeMillis() - start);

        System.out.println("%d requests with %d rate per second took %s".formatted(requests, ratePerSecond, duration));

        System.out.println("Stats in seconds...");

        var min = sortedResults.getFirst();
        var max = sortedResults.getLast();

        var mean = sortedResults.stream().mapToLong(Long::longValue).average().getAsDouble();
        var median = percentile(sortedResults, 50);
        var percentile75 = percentile(sortedResults, 75);
        var percentile90 = percentile(sortedResults, 90);
        var percentile95 = percentile(sortedResults, 95);
        var percentile99 = percentile(sortedResults, 99);
        var percentile999 = percentile(sortedResults, 99.9);

        System.out.println("Min: " + formattedSeconds(min));
        System.out.println("Max: " + formattedSeconds(max));
        System.out.println("Mean: " + formattedSeconds(mean));
        System.out.println("Median: " + formattedSeconds(median));
        System.out.println("Percentile 75: " + formattedSeconds(percentile75));
        System.out.println("Percentile 90: " + formattedSeconds(percentile90));
        System.out.println("Percentile 95: " + formattedSeconds(percentile95));
        System.out.println("Percentile 99: " + formattedSeconds(percentile99));
        System.out.println("Percentile 999: " + formattedSeconds(percentile999));

        System.out.println();

        System.out.println("Completed requests: " + requests);
        System.out.println("Request errors: " + REQUEST_ERRORS.get());
    }

    static int envIntValueOrDefault(String key, int defaultValue) {
        return Integer.parseInt(envValueOrDefault(key, String.valueOf(defaultValue)));
    }

    static String envValueOrDefault(String key, String defaultValue) {
        return System.getenv().getOrDefault(key, defaultValue);
    }

    static List<String> endpoints() {
        var endpoints = envValueOrDefault("ENDPOINTS",
                String.join(",",
                        accountByIdEndpoint(UUID.randomUUID()),
                        accountByIdEndpoint(UUID.randomUUID()),
                        accountByIdEndpoint(UUID.randomUUID()))
        );
        if (endpoints.isEmpty()) {
            throw new RuntimeException("At least one endpoint is required!");
        }
        return Arrays.stream(endpoints.split(",")).map(String::strip).toList();
    }

    static String accountByIdEndpoint(UUID id) {
        return "http://206.189.55.168:80/accounts/" + id;
    }

    static HttpClient newHttpClient(int connectTimeout) {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();
    }

    static long task(HttpClient httpClient, int timeout, List<String> endpoints) {
        var start = System.currentTimeMillis();
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(randomEndpoint(endpoints)))
                    .GET()
                    .timeout(Duration.ofMillis(timeout))
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.out.println("Timeout or another problem!");
            REQUEST_ERRORS.incrementAndGet();
        }
        return System.currentTimeMillis() - start;
    }

    static List<Long> waitForResults(List<Future<Long>> futureResults) {
        return futureResults.stream().map(r -> {
                    try {
                        return r.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .sorted()
                .toList();
    }

    static String randomEndpoint(List<String> endpoints) {
        var idx = RANDOM.nextInt(endpoints.size());
        return endpoints.get(idx);
    }

    static double formattedSeconds(double milliseconds) {
        return milliseconds / 1000;
    }

    static double percentile(List<Long> data, double percentile) {
        if (data.isEmpty()) {
            throw new RuntimeException("no percentile for empty data");
        }

        if (percentile >= 100) {
            return data.getLast();
        }

        if (percentile <= 1) {
            return data.getFirst();
        }

        var index = data.size() * percentile / 100;

        if (isInteger(index)) {
            return data.get((int) index);
        }

        var lowerIdx = (int) Math.floor(index);
        var upperIdx = (int) Math.ceil(index);

        if (lowerIdx < 0) {
            return data.get(upperIdx);
        }

        if (upperIdx >= data.size()) {
            return data.get(lowerIdx);
        }

        return (data.get(lowerIdx) + data.get(upperIdx)) / 2.0;
    }

    static boolean isInteger(double value) {
        return Math.round(value) - value <= 10e6;
    }
}