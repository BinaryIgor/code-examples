import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LoadTest {

    static final TestProfileParams TEST_PROFILE = testProfileParams();
    static final int REQUESTS = envIntValueOrDefault("REQUESTS", TEST_PROFILE.requests());
    static final int REQUESTS_PER_SECOND = envIntValueOrDefault("REQUESTS_PER_SECOND", TEST_PROFILE.requestsPerSecond());
    static final int MAX_CONCURRENCY = envIntValueOrDefault("MAX_CONCURRENCY", TEST_PROFILE.maxConcurrency());
    static final int CONNECT_TIMEOUT = envIntValueOrDefault("CONNECT_TIMEOUT", 5000);
    static final int REQUEST_TIMEOUT = envIntValueOrDefault("REQUEST_TIMEOUT", 5000);
    // Modify these for your custom endpoints to a one host
    static final String HOST = envValueOrDefault("HOST", "http://46.101.207.179:80");
    static final boolean IN_MEMORY_ENDPOINT = Boolean.parseBoolean(envValueOrDefault("IN_MEMORY_ENDPOINT", "false"));
    static final String SECRET_QUERY = envValueOrDefault("SECRET_QUERY", "17e57c8c-60ea-4b4a-8d48-5967f03b942c");
    static final Random RANDOM = new Random();
    static final List<Endpoint> ENDPOINTS = endpoints();
    static final List<String> ENDPOINT_IDS = ENDPOINTS.stream().map(Endpoint::id).distinct().toList();
    static final int MAX_TO_LOG_ISSUES = 10;
    static final AtomicInteger LOGGED_ISSUES = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        var endpointsStats = ENDPOINT_IDS.stream()
                .collect(Collectors.toMap(Function.identity(), k -> EndpointStats.empty()));

        System.out.println("Starting LoadTest!");
        System.out.println("About to make %d requests with %d/s rate".formatted(REQUESTS, REQUESTS_PER_SECOND));
        System.out.println("Timeouts are %d ms for connect and %d ms for request".formatted(CONNECT_TIMEOUT, REQUEST_TIMEOUT));
        System.out.println("Max concurrency is capped at: " + MAX_CONCURRENCY);
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

        var duration = Duration.ofMillis(System.currentTimeMillis() - start);

        printDelimiter();
        System.out.println("%d requests with %d per second rate took %s".formatted(REQUESTS, REQUESTS_PER_SECOND, duration));
        printDelimiter();

        System.out.println("General stats in seconds:");

        var sortedResults = results.stream().map(EndpointResult::time).sorted().toList();
        var connectTimeoutRequests = endpointsStats.values().stream().mapToInt(e -> e.connectTimeoutRequests().get()).sum();
        var requestTimeoutRequests = endpointsStats.values().stream().mapToInt(e -> e.requestTimeoutRequests().get()).sum();
        printStats(sortedResults, connectTimeoutRequests, requestTimeoutRequests);

        printDelimiter();

        ENDPOINT_IDS.forEach(endpointId -> {
            System.out.println(endpointId + " endpoint stats in seconds:");

            var endpointStats = endpointsStats.get(endpointId);

            System.out.println();
            System.out.println(endpointStats);
            printDelimiter();
        });
    }

    static TestProfileParams testProfileParams() {
        var envTestProfile = envValueOrDefault("TEST_PROFILE", TestProfile.LOW_LOAD.name());
        TestProfile testProfile;
        try {
            testProfile = TestProfile.valueOf(envTestProfile);
        } catch (Exception e) {
            throw new RuntimeException(envTestProfile + " is not supported. Supported profiles: "
                    + Arrays.toString(TestProfile.values()));
        }

        return switch (testProfile) {
            case LOW_LOAD -> new TestProfileParams(50, 5);
            case AVERAGE_LOAD -> new TestProfileParams(500, 50);
            case HIGH_LOAD -> new TestProfileParams(5_000, 500);
            case VERY_HIGH_LOAD -> new TestProfileParams(50_000, 5_000);
        };
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

    static List<Endpoint> endpoints() {
        if (IN_MEMORY_ENDPOINT) {
            return List.of(Endpoint.oneInstance("GET: /accounts/in-memory", "GET",
                    EndpointInstance.withoutBody(HOST + "/accounts/in-memory?secret=" + SECRET_QUERY)));
        }

        // Used in AccountController to generate test data!
        var existingId1 = UUID.fromString("06f40771-6460-479a-a47c-177473e240b5");
        var existingId2 = UUID.fromString("4db7506f-43fe-475e-afbe-842514a6223b");
        var accountByIdEndpoint = accountByIdEndpoint(List.of(existingId1, existingId2));

        var existingName1 = "name-1";
        var existingName2 = "name-5";
        var existingName3 = "name-20";
        var countAccountsByNameEndpoint = countAccountsByName(List.of(existingName1, existingName2, existingName3));

        var writeEndpoint = Endpoint.oneInstance(
                "POST: /accounts/execute-random-write",
                "POST",
                EndpointInstance.withoutBody(HOST + "/accounts/execute-random-write?secret=" + SECRET_QUERY));

        return List.of(
                writeEndpoint,
                accountByIdEndpoint,
                accountByIdEndpoint,
                countAccountsByNameEndpoint);
    }

    static Endpoint accountByIdEndpoint(List<UUID> existingIds) {
        var existingInstances = existingIds.stream().map(LoadTest::accountByIdEndpointInstance).toList();
        return new Endpoint("GET: /accounts/{id}", "GET",
                () -> {
                    if (RANDOM.nextBoolean()) {
                        return randomChoice(existingInstances);
                    }
                    var nonExistingId = UUID.randomUUID();
                    return accountByIdEndpointInstance(nonExistingId);
                });
    }

    static EndpointInstance accountByIdEndpointInstance(UUID id) {
        return EndpointInstance.withoutBody(HOST + "/accounts/" + id + "?secret=" + SECRET_QUERY);
    }

    static Endpoint countAccountsByName(List<String> existingNames) {
        var existingInstances = existingNames.stream().map(LoadTest::countAccountsByNameEndpointInstance).toList();
        return new Endpoint("GET: /accounts/count?name={name}", "GET",
                () -> {
                    if (RANDOM.nextBoolean()) {
                        return randomChoice(existingInstances);
                    }
                    var nonExistingName = "name-" + UUID.randomUUID();
                    return countAccountsByNameEndpointInstance(nonExistingName);
                });
    }

    static EndpointInstance countAccountsByNameEndpointInstance(String name) {
        return EndpointInstance.withoutBody(HOST + "/accounts/count?name=%s&secret=%s".formatted(name, SECRET_QUERY));
    }

    static HttpClient newHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT))
                .build();
    }

    static EndpointResult task(HttpClient httpClient, Map<String, EndpointStats> endpointsStats) {
        var start = System.currentTimeMillis();

        var endpoint = randomChoice(ENDPOINTS);
        var endpointStats = endpointsStats.get(endpoint.id());

        endpointStats.incrementRequests();

        try {
            // Make requests more uniform
            randomDelay();

            start = System.currentTimeMillis();
            var endpointInstance = endpoint.instanceSupplier.get();
            var body = endpointInstance.body == null ?
                    HttpRequest.BodyPublishers.noBody() :
                    HttpRequest.BodyPublishers.ofString(endpointInstance.body);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(endpointInstance.url))
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
                endpointStats.incrementConnectTimeoutRequests();
            } else if (e instanceof HttpTimeoutException) {
                endpointStats.incrementRequestTimeoutRequests();
            }
        }

        return new EndpointResult(endpoint.id(), System.currentTimeMillis() - start);
    }

    static void randomDelay() {
        try {
            var randomDelay = RANDOM.nextInt(500);
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
        System.out.println("Executed requests: " + allRequests);
        System.out.println("Requests with connect timeout [%d]: %d, as percentage: %d"
                .formatted(CONNECT_TIMEOUT, connectTimeoutRequests, (connectTimeoutRequests * 100) / allRequests));
        System.out.println("Requests with request timeout [%d]: %d, as percentage: %d"
                .formatted(REQUEST_TIMEOUT, requestTimeoutRequests, (requestTimeoutRequests * 100) / allRequests));

        var min = sortedResults.getFirst();
        var max = sortedResults.getLast();

        var mean = sortedResults.stream().mapToLong(Long::longValue).average().getAsDouble();
        var percentile25 = percentile(sortedResults, 25);
        var percentile50 = percentile(sortedResults, 50);
        var percentile75 = percentile(sortedResults, 75);
        var percentile90 = percentile(sortedResults, 90);
        var percentile95 = percentile(sortedResults, 95);
        var percentile99 = percentile(sortedResults, 99);
        var percentile999 = percentile(sortedResults, 99.9);

        System.out.println("Min: " + formattedSeconds(min));
        System.out.println("Max: " + formattedSeconds(max));
        System.out.println("Mean: " + formattedSeconds(mean));
        System.out.println("Percentile 25: " + formattedSeconds(percentile25));
        System.out.println("Percentile 50 (Median): " + formattedSeconds(percentile50));
        System.out.println("Percentile 75: " + formattedSeconds(percentile75));
        System.out.println("Percentile 90: " + formattedSeconds(percentile90));
        System.out.println("Percentile 95: " + formattedSeconds(percentile95));
        System.out.println("Percentile 99: " + formattedSeconds(percentile99));
        System.out.println("Percentile 999: " + formattedSeconds(percentile999));
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

    enum TestProfile {
        LOW_LOAD, AVERAGE_LOAD, HIGH_LOAD, VERY_HIGH_LOAD
    }

    record TestProfileParams(int requests, int requestsPerSecond, int maxConcurrency) {
        TestProfileParams(int requests, int requestsPerSecond) {
            this(requests, requestsPerSecond, 10_000);
        }
    }

    record EndpointResult(String id, long time) {
    }

    record Endpoint(String id, String method, Supplier<EndpointInstance> instanceSupplier) {

        static Endpoint oneInstance(String id, String method, EndpointInstance instance) {
            return new Endpoint(id, method, () -> instance);
        }
    }

    record EndpointInstance(String url, String body) {

        static EndpointInstance withoutBody(String url) {
            return new EndpointInstance(url, null);
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