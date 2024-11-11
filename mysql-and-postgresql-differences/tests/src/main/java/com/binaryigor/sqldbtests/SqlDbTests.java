package com.binaryigor.sqldbtests;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SqlDbTests {

    static final String TABLE_SINGLE_INDEX = "table_single_index";
    static final String TABLE_FEW_INDEXES = "table_few_indexes";
    static final Random RANDOM = new SecureRandom();
    static final String DATA_SOURCE_NAME = envValueOrThrow("DATA_SOURCE_NAME");
    static final TestCases TEST_CASE;
    static final List<String> STATUSES = List.of("CREATED", "INVITED", "ACTIVATED", "VERIFIED", "BANNED");
    static final String UPPER_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final String RANDOM_STRING_ALPHABET = UPPER_ALPHABET + UPPER_ALPHABET.toLowerCase() + "0123456789";
    static final int MIN_ID = 1;
    static final int MAX_ID = envIntValueOrDefault("MAX_INDEX", 2_000_000);

    static {
        var testCase = envValueOrThrow("TEST_CASE");
        try {
            TEST_CASE = TestCases.valueOf(testCase.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("%s is not supported test case. Supported are: %s".formatted(testCase, Arrays.toString(TestCases.values())));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.printf("Starting db tests, connecting to %s data source%n", DATA_SOURCE_NAME);
        System.out.println();
        var dataSource = dataSource();
        System.out.println();
        System.out.printf("%s data source connected, running %s test case with it!%n", DATA_SOURCE_NAME, TEST_CASE);

        var testCase = testCase();

        System.out.println("The following test case will be executed: " + testCase);

        var tablesCountBeforeTest = tablesCount(dataSource, testCase);
        System.out.println("Tables count before test...");
        tablesCountBeforeTest.forEach((table, count) -> System.out.println(table + ": " + count));
        System.out.println();
        System.out.println("Running it...");
        System.out.println();

        var resultFutures = new LinkedList<Future<Long>>();
        var results = new LinkedList<Long>();

        var executor = Executors.newVirtualThreadPerTaskExecutor();

        var start = System.currentTimeMillis();

        for (var i = 0; i < testCase.queriesToExecute(); i++) {
            var result = executor.submit(() -> queryTest(dataSource, testCase.queryGroups()));
            resultFutures.add(result);

            var issuedQueries = i + 1;
            if (issuedQueries % testCase.queriesMaxRate() == 0 && issuedQueries < testCase.queriesToExecute()) {
                System.out.printf("%s, %d/%d queries were issued, waiting 1s before sending next query batch...%n",
                    LocalDateTime.now(), issuedQueries, testCase.queriesToExecute());
                Thread.sleep(1000);
            }

            if (resultFutures.size() >= testCase.queriesMaxRate()) {
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
        System.out.printf("Test case with %s data source finished! It had queries: %s%n", DATA_SOURCE_NAME, testCase.queryGroups());
        var tablesCountAfterTest = tablesCount(dataSource, testCase);
        System.out.println("Tables count after test...");
        tablesCountAfterTest.forEach((table, count) -> System.out.println(table + ": " + count));
        System.out.println();
        System.out.println("Some stats...");
        System.out.println();

        var sortedResults = results.stream().sorted().toList();
        printStats(sortedResults, duration, testCase.queriesMaxRate());

        printDelimiter();
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

    static DataSource dataSource() {
        var config = new HikariConfig();
        var jdbcUrl = envValueOrThrow("DATA_SOURCE_URL");

        if (jdbcUrl.contains("mysql")) {
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else if (jdbcUrl.contains("postgresql")) {
            config.setDriverClassName("org.postgresql.Driver");
        }

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(envValueOrThrow("DATA_SOURCE_USERNAME"));
        config.setPassword(envValueOrThrow("DATA_SOURCE_PASSWORD"));
        config.setMinimumIdle(envIntValueOrDefault("DATA_SOURCE_POOL_SIZE", 10));
        config.setMaximumPoolSize(envIntValueOrDefault("DATA_SOURCE_POOL_SIZE", 10));
        config.setPoolName(DATA_SOURCE_NAME);

        return new HikariDataSource(config);
    }

    static TestCase testCase() {
        return switch (TEST_CASE) {
            case BATCH_INSERT_TABLE_SINGLE_INDEX -> batchInsertTableSingleIndexTestCase();
            case BATCH_INSERT_TABLE_FEW_INDEXES -> batchInsertTableFewIndexesTestCase();
            case INSERT_TABLE_SINGLE_INDEX -> insertTableSingleIndexTestCase();
            case INSERT_TABLE_FEW_INDEXES -> insertTableFewIndexesTestCase();
            case UPDATE_TABLE_SINGLE_INDEX -> updateTableSingleIndexTestCase();
            case UPDATE_TABLE_FEW_INDEXES -> updateTableFewIndexesTestCase();
            case DELETE_TABLE_SINGLE_INDEX -> deleteTableSingleIndexTestCase();
            case DELETE_TABLE_FEW_INDEXES -> deleteTableFewIndexesTestCase();
            case SELECT_TABLE_SINGLE_INDEX_BY_PRIMARY_KEY -> selectTableSingleIndexByPrimaryKeyTestCase();
            case SELECT_TABLE_FEW_INDEXES_BY_PRIMARY_KEY -> selectTableFewIndexesByPrimaryKeyTestCase();
        };
    }

    static TestCase batchInsertTableSingleIndexTestCase() {
        return new TestCase(
            envQueriesToExecuteOrDefault(2000),
            envQueriesMaxRateOrDefault(10),
            new QueryGroup("batch-insert-table-single-index", TABLE_SINGLE_INDEX,
                () -> insertIntoQuery(TABLE_SINGLE_INDEX, 1000)));
    }

    static TestCase batchInsertTableFewIndexesTestCase() {
        return new TestCase(
            envQueriesToExecuteOrDefault(2000),
            envQueriesMaxRateOrDefault(10),
            List.of(new QueryGroup("batch-insert-table-few-indexes", TABLE_FEW_INDEXES,
                () -> insertIntoQuery(TABLE_FEW_INDEXES, 1000))));
    }

    static int envQueriesToExecuteOrDefault(int defaultValue) {
        return envIntValueOrDefault("QUERIES_TO_EXECUTE", defaultValue);
    }

    static int envQueriesMaxRateOrDefault(int defaultValue) {
        return envIntValueOrDefault("QUERIES_MAX_RATE", defaultValue);
    }

    static String insertIntoQuery(String table, int records) {
        return "INSERT INTO %s (name, status, created_at, updated_at, version) VALUES ".formatted(table)
               + Stream.generate(() -> {
                var name = randomName();
                var status = randomChoice(STATUSES);
                var createdAt = randomTimestamp();
                var updatedAt = createdAt.plusSeconds(RANDOM.nextLong(Duration.ofHours(12).toSeconds()));
                var version = 1;
                return "('%s', '%s', '%s', '%s', %d)"
                    .formatted(name, status, timestampToString(createdAt), timestampToString(updatedAt), version);
            }
        ).limit(records).collect(Collectors.joining(",\n"));
    }

    static String randomName() {
        return randomString(10, 50);
    }

    static String randomString(int minSize, int maxSize) {
        int size;
        if (minSize == maxSize) {
            size = maxSize;
        } else {
            size = minSize + RANDOM.nextInt(maxSize - minSize);
        }
        var builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            var idx = RANDOM.nextInt(RANDOM_STRING_ALPHABET.length());
            builder.append(RANDOM_STRING_ALPHABET.charAt(idx));
        }
        return builder.toString();
    }

    static Instant randomTimestamp() {
        return Instant.now().minusSeconds(RANDOM.nextLong(Duration.ofDays(1).getSeconds()));
    }

    // MySQL doesn't like Z
    static String timestampToString(Instant instant) {
        return instant.truncatedTo(ChronoUnit.MICROS).toString().replace("Z", "");
    }

    static TestCase insertTableSingleIndexTestCase() {
        return new TestCase(
            envQueriesToExecuteOrDefault(5000),
            envQueriesMaxRateOrDefault(50),
            new QueryGroup("insert-table-single-index", TABLE_SINGLE_INDEX,
                () -> insertIntoQuery(TABLE_SINGLE_INDEX, 1)));
    }

    static TestCase insertTableFewIndexesTestCase() {
        return new TestCase(
            envQueriesToExecuteOrDefault(5000),
            envQueriesMaxRateOrDefault(50),
            new QueryGroup("insert-table-few-indexes", TABLE_FEW_INDEXES,
                () -> insertIntoQuery(TABLE_FEW_INDEXES, 1)));
    }

    static TestCase updateTableSingleIndexTestCase() {
        return new TestCase(
            envQueriesToExecuteOrDefault(5000),
            envQueriesMaxRateOrDefault(50),
            new QueryGroup("update-table-single-index", TABLE_SINGLE_INDEX,
                () -> updateQuery(TABLE_SINGLE_INDEX)));
    }

    static TestCase updateTableFewIndexesTestCase() {
        return new TestCase(
            envQueriesToExecuteOrDefault(5000),
            envQueriesMaxRateOrDefault(50),
            new QueryGroup("update-table-few-indexes", TABLE_FEW_INDEXES,
                () -> updateQuery(TABLE_FEW_INDEXES)));
    }

    static String updateQuery(String table) {
        var newName = randomName();
        var newVersion = 1 + RANDOM.nextInt(100_000);
        var updatedAt = timestampToString(randomTimestamp());
        var id = MIN_ID + RANDOM.nextInt(MAX_ID);
        return "UPDATE %s set name = '%s', updated_at = '%s', version= %d WHERE id = %d"
            .formatted(table, newName, updatedAt, newVersion, id);
    }

    static TestCase deleteTableSingleIndexTestCase() {
        return new TestCase(
            envQueriesToExecuteOrDefault(5000),
            envQueriesMaxRateOrDefault(50),
            new QueryGroup("delete-table-single-index", TABLE_SINGLE_INDEX,
                () -> deleteQuery(TABLE_SINGLE_INDEX)));
    }

    static TestCase deleteTableFewIndexesTestCase() {
        return new TestCase(
            envQueriesToExecuteOrDefault(5000),
            envQueriesMaxRateOrDefault(50),
            new QueryGroup("delete-table-few-indexes", TABLE_FEW_INDEXES,
                () -> deleteQuery(TABLE_FEW_INDEXES)));
    }

    static String deleteQuery(String table) {
        var id = MIN_ID + RANDOM.nextInt(MAX_ID);
        return "DELETE FROM %s WHERE id = %d".formatted(table, id);
    }

    static TestCase selectTableSingleIndexByPrimaryKeyTestCase() {
        return new TestCase(
            envQueriesToExecuteOrDefault(10_000),
            envQueriesMaxRateOrDefault(100),
            new QueryGroup("select-table-single-index-by-primary-key", TABLE_SINGLE_INDEX,
                () -> selectByIdQuery(TABLE_SINGLE_INDEX)));
    }

    static TestCase selectTableFewIndexesByPrimaryKeyTestCase() {
        return new TestCase(
            envQueriesToExecuteOrDefault(10_000),
            envQueriesMaxRateOrDefault(100),
            List.of(new QueryGroup("select-table-few-indexes-by-primary-key", TABLE_FEW_INDEXES,
                () -> selectByIdQuery(TABLE_FEW_INDEXES))));
    }

    static String selectByIdQuery(String table) {
        var id = MIN_ID + RANDOM.nextInt(MAX_ID);
        return "SELECT * FROM %s WHERE id = %d".formatted(table, id);
    }

    static <T> T randomChoice(List<T> elements) {
        var idx = RANDOM.nextInt(elements.size());
        return elements.get(idx);
    }

    static long queryTest(DataSource dataSource,
                          List<QueryGroup> queryGroups) {
        // Make queries more uniform
        randomDelay();

        try (var conn = dataSource.getConnection()) {
            var start = System.nanoTime();
            var group = randomChoice(queryGroups);

            var query = group.query();

            var statement = conn.createStatement();
            var result = statement.execute(query);

            if (result) {
                var resultSet = statement.getResultSet();
                while (resultSet.next()) {
                    // consuming result to make select measures objective
                }
            }

            return System.nanoTime() - start;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    static Map<String, Integer> tablesCount(DataSource dataSource, TestCase testCase) {
        return testCase.queryGroups().stream()
            .flatMap(q -> q.tables.stream())
            .distinct()
            .collect(Collectors.toMap(Function.identity(), table -> tableCount(dataSource, table)));
    }

    static int tableCount(DataSource dataSource, String table) {
        try (var conn = dataSource.getConnection()) {
            var result = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + table);
            result.next();
            return result.getInt(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void randomDelay() {
        try {
            var randomDelay = RANDOM.nextInt(1000);
            Thread.sleep(randomDelay);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    static void printStats(List<Long> sortedResults, Duration duration, int queriesRate) {
        var min = sortedResults.getFirst();
        var max = sortedResults.getLast();

        System.out.println("Test duration: " + duration);
        System.out.println("Executed queries: " + sortedResults.size());
        System.out.printf("Queries rate: %d/s%n", queriesRate);
        System.out.println();

        var mean = sortedResults.stream().mapToLong(Long::longValue).average().getAsDouble();
        var percentile50 = percentile(sortedResults, 50);
        var percentile75 = percentile(sortedResults, 75);
        var percentile90 = percentile(sortedResults, 90);
        var percentile95 = percentile(sortedResults, 95);
        var percentile99 = percentile(sortedResults, 99);
        var percentile999 = percentile(sortedResults, 99.9);

        System.out.println("Min: " + formattedMillis(min));
        System.out.println("Max: " + formattedMillis(max));
        System.out.println("Mean: " + formattedMillis(mean));
        System.out.println();
        System.out.println("Percentile 50 (Median): " + formattedMillis(percentile50));
        System.out.println("Percentile 75: " + formattedMillis(percentile75));
        System.out.println("Percentile 90: " + formattedMillis(percentile90));
        System.out.println("Percentile 95: " + formattedMillis(percentile95));
        System.out.println("Percentile 99: " + formattedMillis(percentile99));
        System.out.println("Percentile 99.9: " + formattedMillis(percentile999));
    }

    static String formattedMillis(double nanos) {
        return (Math.round(nanos) / 1_000_000.0) + " ms";
    }

    static double percentile(List<Long> data, double percentile) {
        if (data.isEmpty()) {
            throw new RuntimeException("No percentile for empty data");
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

    enum TestCases {
        INSERT_TABLE_SINGLE_INDEX,
        INSERT_TABLE_FEW_INDEXES,
        BATCH_INSERT_TABLE_SINGLE_INDEX,
        BATCH_INSERT_TABLE_FEW_INDEXES,
        UPDATE_TABLE_SINGLE_INDEX,
        UPDATE_TABLE_FEW_INDEXES,
        DELETE_TABLE_SINGLE_INDEX,
        DELETE_TABLE_FEW_INDEXES,
        SELECT_TABLE_SINGLE_INDEX_BY_PRIMARY_KEY,
        SELECT_TABLE_FEW_INDEXES_BY_PRIMARY_KEY
    }

    record TestCase(int queriesToExecute,
                    int queriesMaxRate,
                    List<QueryGroup> queryGroups) {

        TestCase(int queriesToExecute, int maxRate, QueryGroup queryGroup) {
            this(queriesToExecute, maxRate, List.of(queryGroup));
        }
    }

    static class QueryGroup {

        final String id;
        final Collection<String> tables;
        private final Supplier<String> query;

        QueryGroup(String id, Collection<String> tables, Supplier<String> query) {
            this.id = id;
            this.tables = tables;
            this.query = query;
        }

        QueryGroup(String id, String table, Supplier<String> query) {
            this(id, List.of(table), query);
        }

        String query() {
            return query.get();
        }

        @Override
        public String toString() {
            return "QueryGroup[id=%s, tables=%s]".formatted(id, tables);
        }
    }
}
