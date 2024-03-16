import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LoadTest {

    static final int REQUESTS = envIntValueOrDefault("REQUESTS", 100);
    static final int REQUESTS_PER_SECOND = envIntValueOrDefault("REQUESTS_PER_SECOND", 10);
    static final int MAX_CONCURRENCY = envIntValueOrDefault("MAX_CONCURRENCY", 5000);
    static final int CONNECT_TIMEOUT = envIntValueOrDefault("CONNECT_TIMEOUT", 1000);
    static final int REQUEST_TIMEOUT = envIntValueOrDefault("REQUEST_TIMEOUT", 5000);
    // Modify these ones for your custom endpoints to a one host
    static final String HOST = envValueOrDefault("HOST", "http://164.92.167.184:80");
    static final String SECRET_QUERY = envValueOrDefault("SECRET_QUERY", "17e57c8c-60ea-4b4a-8d48-5967f03b942c");
    static final List<Endpoint> ENDPOINTS = endpoints(HOST);

    static final Random RANDOM = new Random();

    static final int MAX_TO_LOG_ISSUES = 100;
    static final AtomicInteger LOGGED_ISSUES = new AtomicInteger(0);
    static final AtomicLong REQUESTS_TOTAL = new AtomicLong(0);
    static final AtomicLong REQUEST_CONNECT_TIMEOUTS_TOTAL = new AtomicLong(0);

    public static void main(String[] args) throws Exception {
        var endpointsStats = ENDPOINTS.stream()
                .collect(Collectors.toMap(Function.identity(), k -> EndpointStats.empty()));

        System.out.println("Starting LoadTest!");
        System.out.println("About to make %d requests with %d/s rate".formatted(REQUESTS, REQUESTS_PER_SECOND));
        System.out.println("Timeouts are %d ms for connect and %d ms for request".formatted(CONNECT_TIMEOUT, REQUEST_TIMEOUT));
        System.out.println("Max concurrency is capped at: " + MAX_CONCURRENCY);
        System.out.println("Endpoints to test (chosen randomly):");
        ENDPOINTS.forEach(System.out::println);
        printDelimiter();

        var start = System.currentTimeMillis();

        var httpClient = newHttpClient();

        var executor = Executors.newVirtualThreadPerTaskExecutor();

        var resultFutures = new LinkedList<Future<Long>>();
        var results = new LinkedList<Long>();

        for (var i = 0; i < REQUESTS; i++) {
            var result = executor.submit(() -> task(httpClient, endpointsStats));
            resultFutures.add(result);

            if (resultFutures.size() >= MAX_CONCURRENCY) {
                results.addAll(getFutureResults(resultFutures));
                resultFutures.clear();
            }

            var issuedRequests = i + 1;
            if (issuedRequests % REQUESTS_PER_SECOND == 0 && issuedRequests < REQUESTS) {
                System.out.println("%s, %d/%d requests were issued, waiting 1s for the next packet..."
                        .formatted(LocalDateTime.now(), issuedRequests, REQUESTS));
                Thread.sleep(1000);
            }
        }

        if (!resultFutures.isEmpty()) {
            results.addAll(getFutureResults(resultFutures));
            resultFutures.clear();
        }

        var sortedResults = results.stream().sorted().toList();

        var duration = Duration.ofMillis(System.currentTimeMillis() - start);

        printDelimiter();
        System.out.println("%d requests with %d per second rate took %s".formatted(REQUESTS, REQUESTS_PER_SECOND, duration));
        printDelimiter();

        System.out.println("Stats in seconds:");

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
        System.out.println("Total requests: " + REQUESTS_TOTAL.get());
        System.out.println("Total requests with connect timeout: " + REQUEST_CONNECT_TIMEOUTS_TOTAL.get());
        System.out.println("Total requests with connect timeout %: "
                + REQUEST_CONNECT_TIMEOUTS_TOTAL.get() * 100 / REQUESTS_TOTAL.get());

        printDelimiter();

        System.out.println("Endpoints stats:");
        endpointsStats.forEach((k, v) -> {
            System.out.println();
            System.out.println(k);
            System.out.println(v);
        });
    }

    static void printDelimiter() {
        System.out.println();
        System.out.println("...");
        System.out.println();
    }

    static int envIntValueOrDefault(String key, int defaultValue) {
        return Integer.parseInt(envValueOrDefault(key, String.valueOf(defaultValue)));
    }

    static String envValueOrDefault(String key, String defaultValue) {
        return System.getenv().getOrDefault(key, defaultValue);
    }

    static List<Endpoint> endpoints(String host) {
        // Used in AccountController to generate test data!
        var existingId1 = UUID.fromString("06f40771-6460-479a-a47c-177473e240b5");
        var existingId2 = UUID.fromString("4db7506f-43fe-475e-afbe-842514a6223b");
        return List.of(
                Endpoint.withoutBody("POST", host + "/accounts/execute-random-write" + SECRET_QUERY),
                accountByIdEndpoint(host, UUID.randomUUID()),
                accountByIdEndpoint(host, existingId1),
                accountByIdEndpoint(host, existingId2));
    }

    static Endpoint accountByIdEndpoint(String host, UUID id) {
        return Endpoint.withoutBody("GET", host + "/accounts/" + id + "?secret=" + SECRET_QUERY);
    }

    static HttpClient newHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT))
                .build();
    }

    static long task(HttpClient httpClient, Map<Endpoint, EndpointStats> endpointsStats) {
        var start = System.currentTimeMillis();

        var endpoint = randomEndpoint(ENDPOINTS);
        var endpointStats = endpointsStats.get(endpoint);

        endpointStats.incrementRequests();

        try {
            var body = endpoint.body == null ?
                    HttpRequest.BodyPublishers.noBody() :
                    HttpRequest.BodyPublishers.ofString(endpoint.body);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint.url))
                    .method(endpoint.method, body)
                    .timeout(Duration.ofMillis(REQUEST_TIMEOUT))
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            endpointStats.incrementRequestStatus(response.statusCode());
        } catch (Exception e) {
            if (LOGGED_ISSUES.getAndIncrement() < MAX_TO_LOG_ISSUES) {
                System.out.println("Timeout or another issue during request!");
                e.printStackTrace();
            }
            if (e instanceof HttpConnectTimeoutException) {
                REQUEST_CONNECT_TIMEOUTS_TOTAL.incrementAndGet();
            }
            endpointStats.incrementExceptionRequests();
        }

        REQUESTS_TOTAL.incrementAndGet();

        return System.currentTimeMillis() - start;
    }

    static List<Long> getFutureResults(List<Future<Long>> futureResults) {
        return futureResults.stream()
                .map(r -> {
                    try {
                        return r.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    static Endpoint randomEndpoint(List<Endpoint> endpoints) {
        var idx = RANDOM.nextInt(endpoints.size());
        return endpoints.get(idx);
    }

    static double formattedSeconds(double milliseconds) {
        return Math.round(milliseconds) / 1000.0;
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

    record Endpoint(String method, String url, String body) {

        static Endpoint withoutBody(String method, String url) {
            return new Endpoint(method, url, null);
        }
    }

    record EndpointStats(AtomicInteger requests,
                         AtomicInteger exceptionRequests,
                         Map<Integer, AtomicInteger> requestsByStatus) {

        static EndpointStats empty() {
            return new EndpointStats(new AtomicInteger(0),
                    new AtomicInteger(0),
                    new ConcurrentHashMap<>());
        }

        void incrementRequests() {
            requests.getAndIncrement();
        }

        void incrementExceptionRequests() {
            exceptionRequests.getAndIncrement();
        }

        void incrementRequestStatus(int status) {
            requestsByStatus.computeIfAbsent(status, k -> new AtomicInteger(0)).getAndIncrement();
        }
    }
}