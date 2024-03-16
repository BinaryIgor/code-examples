package src;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoadTest {

    static final Random RANDOM = new Random();

    public static void main(String[] args) throws Exception {
        var requests = envIntValueOrDefault("REQUESTS", 100);
        var ratePerSecond = envIntValueOrDefault("RATE_PER_SECOND", 10);
        var maxPendingFutures = envIntValueOrDefault("MAX_PENDING_FUTURES", 500);
        var endpoints = endpoints();

        System.out.println("About to make %d requests, with %d/s rate to %s endpoints..."
                .formatted(requests, ratePerSecond, endpoints));

        var start = System.currentTimeMillis();

        var httpClient = newHttpClient();

        var executor = Executors.newVirtualThreadPerTaskExecutor();

        var resultFutures = new LinkedList<Future<Long>>();
        var results = new LinkedList<Long>();

        for (var i = 0; i < requests; i++) {
            var result = executor.submit(() -> task(httpClient, endpoints));
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
    }

    static int envIntValueOrDefault(String key, int defaultValue) {
        return Integer.parseInt(envValueOrDefault(key, String.valueOf(defaultValue)));
    }

    static String envValueOrDefault(String key, String defaultValue) {
        return System.getenv().getOrDefault(key, defaultValue);
    }

    static List<String> endpoints() {
        var endpoints = envValueOrDefault("ENDPOINTS", "");
        if (endpoints.isEmpty()) {
            throw new RuntimeException("At least one endpoint is required!");
        }
        return Arrays.stream(endpoints.split(",")).toList();
    }

    static HttpClient newHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    static long task(HttpClient httpClient, List<String> endpoints) {
        var start = System.currentTimeMillis();
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(randomEndpoint(endpoints)))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.out.println("Timeout or another problem!");
            e.printStackTrace();
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