import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CdnLoadTest {

    static final int REQUESTS = envIntValueOrDefault("REQUESTS", 2000);
    static final int REQUESTS_PER_SECOND = envIntValueOrDefault("REQUESTS_PER_SECOND", 50);
    static final int MAX_CONCURRENCY = envIntValueOrDefault("MAX_CONCURRENCY", 100);
    static final int CONNECT_TIMEOUT = envIntValueOrDefault("CONNECT_TIMEOUT", 5000);
    static final int REQUEST_TIMEOUT = envIntValueOrDefault("REQUEST_TIMEOUT", 5000);
    static final String HOST = envValueOrThrow("HOST");
    static final Random RANDOM = new Random();
    static final List<Endpoint> ENDPOINTS = endpoints();
    static final int MAX_TO_LOG_ISSUES = 10;
    static final AtomicInteger LOGGED_ISSUES = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        var endpointsStats = ENDPOINTS.stream()
            .collect(Collectors.toMap(Endpoint::id, k -> EndpointStats.empty()));

        System.out.println("Starting CdnDifferenceTest!");
        System.out.println();
        System.out.printf("About to make %d requests with %d/s rate to %s host%n", REQUESTS, REQUESTS_PER_SECOND, HOST);
        System.out.printf("Timeouts are %d ms for connect and %d ms for request%n", CONNECT_TIMEOUT, REQUEST_TIMEOUT);
        System.out.println("Max concurrency is capped at: " + MAX_CONCURRENCY);
        System.out.println();
        System.out.println("Endpoints to test (chosen randomly):");
        ENDPOINTS.forEach(e -> System.out.println(e.id()));
        printDelimiter();

        var start = System.currentTimeMillis();

        var httpClient = newHttpClient();

        var executor = Executors.newVirtualThreadPerTaskExecutor();

        var resultFutures = new LinkedList<Future<EndpointResult>>();
        var results = new LinkedList<EndpointResult>();

        for (var i = 0; i < REQUESTS; i++) {
            var result = executor.submit(() -> task(httpClient, endpointsStats));
            resultFutures.add(result);

            var issuedRequests = i + 1;
            if (issuedRequests % REQUESTS_PER_SECOND == 0 && issuedRequests < REQUESTS) {
                System.out.printf("%s, %d/%d requests were issued, waiting 1s before sending next batch...%n", LocalDateTime.now(), issuedRequests, REQUESTS);
                Thread.sleep(1000);
            }

            if (resultFutures.size() >= MAX_CONCURRENCY) {
                results.addAll(getFutureResults(resultFutures));
                resultFutures.clear();
            }
        }

        if (!resultFutures.isEmpty()) {
            results.addAll(getFutureResults(resultFutures));
            resultFutures.clear();
        }

        var duration = Duration.ofMillis(System.currentTimeMillis() - start);

        printDelimiter();
        System.out.printf("%d requests with %d per second rate took %s%n", REQUESTS, REQUESTS_PER_SECOND, duration);
        printDelimiter();

        var sortedResults = results.stream().map(EndpointResult::time).sorted().toList();
        var connectTimeoutRequests = endpointsStats.values().stream().mapToInt(e -> e.connectTimeoutRequests().get()).sum();
        var requestTimeoutRequests = endpointsStats.values().stream().mapToInt(e -> e.requestTimeoutRequests().get()).sum();
        printStats(sortedResults, connectTimeoutRequests, requestTimeoutRequests);

        printDelimiter();

        ENDPOINTS.forEach(endpoint -> {
            var endpointStats = endpointsStats.get(endpoint.id());
            printEndpointStats(endpoint.id(), endpointStats, sortedResults.size());
            printDelimiter();
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

    static String envValueOrThrow(String key) {
        return Optional.ofNullable(System.getenv().get(key))
            .orElseThrow(() -> new RuntimeException("%s env variable is required but was not supplied!".formatted(key)));
    }

    static List<Endpoint> endpoints() {
        return List.of(new Endpoint("GET", "index.html"),
            new Endpoint("GET", "styles.css"),
            new Endpoint("GET", "cdn.png"));
    }

    static HttpClient newHttpClient() {
        return HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT))
            .build();
    }

    static EndpointResult task(HttpClient httpClient, Map<String, EndpointStats> endpointsStats) {
        // Make requests more uniform
        randomDelay();

        var start = System.currentTimeMillis();

        var endpoint = randomChoice(ENDPOINTS);
        var endpointStats = endpointsStats.get(endpoint.id());

        endpointStats.incrementRequests();

        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(HOST + "/" + endpoint.path))
                .method(endpoint.method, HttpRequest.BodyPublishers.noBody())
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
                endpointStats.incrementConnectTimeoutRequests();
            } else if (e instanceof HttpTimeoutException) {
                endpointStats.incrementRequestTimeoutRequests();
            }
        }

        return new EndpointResult(endpoint.id(), System.currentTimeMillis() - start);
    }

    static void randomDelay() {
        try {
            var randomDelay = RANDOM.nextInt(1000);
            Thread.sleep(randomDelay);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static List<EndpointResult> getFutureResults(List<Future<EndpointResult>> futureResults) {
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

    static <T> T randomChoice(List<T> elements) {
        var idx = RANDOM.nextInt(elements.size());
        return elements.get(idx);
    }

    static void printStats(List<Long> sortedResults,
                           int connectTimeoutRequests,
                           int requestTimeoutRequests) {

        var allRequests = sortedResults.size();

        System.out.printf("Executed requests: %d, with %d/s rate%n", allRequests, REQUESTS_PER_SECOND);
        System.out.printf("Requests with connect timeout [%d]: %d, as percentage: %d%n", CONNECT_TIMEOUT, connectTimeoutRequests, (connectTimeoutRequests * 100) / allRequests);
        System.out.printf("Requests with request timeout [%d]: %d, as percentage: %d%n", REQUEST_TIMEOUT, requestTimeoutRequests, (requestTimeoutRequests * 100) / allRequests);
        System.out.println();

        var min = sortedResults.getFirst();
        var max = sortedResults.getLast();

        var mean = sortedResults.stream().mapToLong(Long::longValue).average().getAsDouble();
        var percentile10 = percentile(sortedResults, 10);
        var percentile25 = percentile(sortedResults, 25);
        var percentile50 = percentile(sortedResults, 50);
        var percentile75 = percentile(sortedResults, 75);
        var percentile90 = percentile(sortedResults, 90);
        var percentile95 = percentile(sortedResults, 95);
        var percentile99 = percentile(sortedResults, 99);

        System.out.println("Min: " + formattedSeconds(min));
        System.out.println("Max: " + formattedSeconds(max));
        System.out.println("Mean: " + formattedSeconds(mean));
        System.out.println();
        System.out.println("Percentile 10: " + formattedSeconds(percentile10));
        System.out.println("Percentile 25: " + formattedSeconds(percentile25));
        System.out.println("Percentile 50 (Median): " + formattedSeconds(percentile50));
        System.out.println("Percentile 75: " + formattedSeconds(percentile75));
        System.out.println("Percentile 90: " + formattedSeconds(percentile90));
        System.out.println("Percentile 95: " + formattedSeconds(percentile95));
        System.out.println("Percentile 99: " + formattedSeconds(percentile99));
    }

    static String formattedSeconds(double milliseconds) {
        return (Math.round(milliseconds) / 1000.0) + " s";
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

    static void printEndpointStats(String endpointId, EndpointStats endpointStats, int allRequests) {
        System.out.println(endpointId);
        System.out.printf("Requests: %d, which is %s of all requests%n", endpointStats.requests.get(),
            formattedPercentage(endpointStats.requests.get(), allRequests));
        System.out.println("Connect timeouts: " + endpointStats.connectTimeoutRequests);
        System.out.println("Request timeouts: " + endpointStats.requestTimeoutRequests);
        System.out.println("Requests by status: " + endpointStats.requestsByStatus);
    }

    static String formattedPercentage(int number, int total) {
        return Math.round(number * 100.0 / total) + "%";
    }

    record EndpointResult(String id, long time) {
    }

    record Endpoint(String method, String path) {
        String id() {
            return method + ":" + path;
        }
    }


    record EndpointStats(AtomicInteger requests,
                         AtomicInteger connectTimeoutRequests,
                         AtomicInteger requestTimeoutRequests,
                         Map<Integer, AtomicInteger> requestsByStatus) {

        static EndpointStats empty() {
            return new EndpointStats(new AtomicInteger(0),
                new AtomicInteger(0),
                new AtomicInteger(0),
                new ConcurrentHashMap<>());
        }

        void incrementRequests() {
            requests.getAndIncrement();
        }

        void incrementConnectTimeoutRequests() {
            connectTimeoutRequests.getAndIncrement();
        }

        void incrementRequestTimeoutRequests() {
            requestTimeoutRequests.getAndIncrement();
        }

        void incrementRequestStatus(int status) {
            requestsByStatus.computeIfAbsent(status, k -> new AtomicInteger(0)).getAndIncrement();
        }
    }
}