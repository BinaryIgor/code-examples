import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MpaVsSpaLoadTest {

    static final int REQUESTS = envIntValueOrDefault("REQUESTS", 1000);
    static final int REQUESTS_PER_SECOND = envIntValueOrDefault("REQUESTS_PER_SECOND", 100);
    static final int MAX_CONCURRENCY = envIntValueOrDefault("MAX_CONCURRENCY", 200);
    static final int CONNECT_TIMEOUT = envIntValueOrDefault("CONNECT_TIMEOUT", 5000);
    static final int REQUEST_TIMEOUT = envIntValueOrDefault("REQUEST_TIMEOUT", 5000);
    static final String HOST = envValueOrThrow("HOST");
    static final TestCase TEST_CASE;
    static final List<Endpoint> ENDPOINTS;
    static final Random RANDOM = new Random();
    static final int MAX_TO_LOG_ISSUES = 10;
    static final AtomicInteger LOGGED_ISSUES = new AtomicInteger(0);
    static final String TOKEN = envValueOrThrow("TOKEN");

    static {
        var testCase = envValueOrThrow("TEST_CASE");
        try {
            TEST_CASE = TestCase.valueOf(testCase.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException(testCase + " TestCase is not supported. Supported are: " +
                                       Arrays.toString(TestCase.values()));
        }
        ENDPOINTS = endpoints();
    }

    public static void main(String[] args) throws Exception {
        var endpointsStats = ENDPOINTS.stream().collect(Collectors.toMap(Endpoint::id, k -> EndpointStats.empty(), (p, k) -> p));

        System.out.println("Starting MpaVsSpaLoadTest!");
        System.out.println();
        System.out.printf("About to make %d requests with %d/s rate to %s host%n", REQUESTS, REQUESTS_PER_SECOND, HOST);
        System.out.printf("Timeouts are %d ms for connect and %d ms for request%n", CONNECT_TIMEOUT, REQUEST_TIMEOUT);
        System.out.println("Max concurrency is capped at: " + MAX_CONCURRENCY);
        System.out.println();
        System.out.println("Endpoints to test (chosen randomly):");
        endpointsStats.keySet().forEach(System.out::println);
        printDelimiter();

        var start = System.currentTimeMillis();

        var httpClient = newHttpClient();

        var results = new LinkedList<EndpointResult>();
        var resultFutures = new LinkedList<Future<EndpointResult>>();

        var executor = Executors.newVirtualThreadPerTaskExecutor();

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

        endpointsStats.forEach((id, stats) -> {
            var endpointSortedResults = results.stream().filter(r -> r.id().equals(id)).map(EndpointResult::time).sorted().toList();
            printEndpointStats(id, stats, sortedResults.size(), endpointSortedResults);
            printDelimiter();
        });
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

    static void printDelimiter() {
        System.out.println();
        System.out.println("...");
        System.out.println();
    }

    static List<Endpoint> endpoints() {
        // TODO: handle compression
        var jsPath = envValueOrThrow("JS_PATH");
        var cssPath = envValueOrThrow("CSS_PATH");
        return switch (TEST_CASE) {
            case MPA -> {
                yield Stream.of(
                    Stream.of(new Endpoint("GET", jsPath), new Endpoint("GET", cssPath)),
                    Stream.generate(() -> new Endpoint("GET", "projects")).limit(3),
                    Stream.generate(() -> new Endpoint("GET", "tasks")).limit(3),
                    Stream.generate(() -> new Endpoint("GET", "account")).limit(3)
                ).flatMap(s -> s).toList();
            }
            case SPA -> {
                yield Stream.of(
                    Stream.of(new Endpoint("GET", jsPath), new Endpoint("GET", cssPath)),
                    Stream.generate(() -> new Endpoint("GET", "/api/projects")).limit(3),
                    Stream.generate(() -> new Endpoint("GET", "/api/tasks")).limit(3),
                    Stream.generate(() -> new Endpoint("GET", "/api/user-info")).limit(3)
                ).flatMap(s -> s).toList();
            }
        };
    }

    static HttpClient newHttpClient() throws Exception {
        var builder = HttpClient.newBuilder();

        var disableCertsVerification = Boolean.parseBoolean(envValueOrDefault("DISABLE_CERTS_VERIFICATION", "false")) || HOST.contains("localhost");
        if (disableCertsVerification) {
            // For self-signed certs on localhost to work
            System.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());

            TrustManager trustAllCertificates = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            var sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustAllCertificates}, new SecureRandom());

            builder.sslContext(sslContext);
        }

        return builder
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
                .uri(URI.create(HOST + "/" + endpoint.path()))
                .method(endpoint.method, HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofMillis(REQUEST_TIMEOUT))
                .header("Cookie", "token=%s".formatted(TOKEN))
                .header("Accept-Encoding", "gzip, br")
                .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

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

        printStats(sortedResults);
    }

    static void printStats(List<Long> sortedResults) {
        var min = sortedResults.getFirst();
        var max = sortedResults.getLast();

        var mean = sortedResults.stream().mapToLong(Long::longValue).average().getAsDouble();
        var percentile50 = percentile(sortedResults, 50);
        var percentile75 = percentile(sortedResults, 75);
        var percentile90 = percentile(sortedResults, 90);
        var percentile99 = percentile(sortedResults, 99);

        System.out.println("Min: " + formattedSeconds(min));
        System.out.println("Max: " + formattedSeconds(max));
        System.out.println("Mean: " + formattedSeconds(mean));
        System.out.println();
        System.out.println("Percentile 50 (Median): " + formattedSeconds(percentile50));
        System.out.println("Percentile 75: " + formattedSeconds(percentile75));
        System.out.println("Percentile 90: " + formattedSeconds(percentile90));
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

    static void printEndpointStats(String endpointId, EndpointStats endpointStats,
                                   int allRequests, List<Long> sortedResults) {
        System.out.println(endpointId);
        System.out.printf("Requests: %d, which is %s of all requests%n", endpointStats.requests.get(),
            formattedPercentage(endpointStats.requests.get(), allRequests));
        System.out.println("Connect timeouts: " + endpointStats.connectTimeoutRequests);
        System.out.println("Request timeouts: " + endpointStats.requestTimeoutRequests);
        System.out.println("Requests by status: " + endpointStats.requestsByStatus);
        System.out.println();
        printStats(sortedResults);
    }

    static String formattedPercentage(int number, int total) {
        return Math.round(number * 100.0 / total) + "%";
    }

    enum TestCase {
        MPA, SPA
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