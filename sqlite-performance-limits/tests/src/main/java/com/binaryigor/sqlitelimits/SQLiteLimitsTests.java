package com.binaryigor.sqlitelimits;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SQLiteLimitsTests {

    static final String TABLE = "account";
    static final String SCHEMA = """
        CREATE TABLE IF NOT EXISTS %s (
          id INTEGER PRIMARY KEY,
          email TEXT NOT NULL UNIQUE,
          name TEXT NOT NULL,
          description TEXT,
          created_at INTEGER NOT NULL,
          version INTEGER
        );
        CREATE INDEX IF NOT EXISTS account_name ON %s(name);
        """.formatted(TABLE, TABLE).trim();

    static final Random RANDOM = new SecureRandom();
    static final String UPPER_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final String RANDOM_STRING_ALPHABET = UPPER_ALPHABET + UPPER_ALPHABET.toLowerCase() + "0123456789";
    static final TestCases TEST_CASE;
    static final String DB_PATH = Path.of(envValueOrThrow("DB_DIRECTORY"), "limits_tests.db").toString();
    // DELETE for default journal, WAL for Write Ahead Logging. Reference: https://www.sqlite.org/pragma.html#pragma_journal_mode
    static final String JOURNAL_MODE = envValueOrDefault("JOURNAL_MODE", "DELETE");
    static final String BEFORE_TESTS_QUERY = envValueOrDefault("BEFORE_TESTS_QUERY", "");

    static {
        var testCase = envValueOrThrow("TEST_CASE");
        try {
            TEST_CASE = TestCases.valueOf(testCase.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("%s is not supported test case. Supported are: %s".formatted(testCase, Arrays.toString(TestCases.values())));
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("About to run SQLite Limits Tests!");
        System.out.printf("Connecting to %s db with the journal_mode=%s%n", DB_PATH, JOURNAL_MODE);
        System.out.println();

        var dataSource = dataSource();

        System.out.println();
        System.out.println("DB connection established, running init queries");
        runInitQueries(dataSource);
        System.out.println();
        System.out.println("Init queries executed, db schema:");
        System.out.println();
        System.out.println(SCHEMA);
        System.out.println();

        var testCase = testCase(dataSource);

        System.out.println("The following test case (%s) will be executed: ".formatted(TEST_CASE) + testCase);

        System.out.println();
        var tablesCountBeforeTest = tablesCount(dataSource, testCase);
        System.out.println("Tables count before test...");
        tablesCountBeforeTest.forEach((table, count) -> System.out.println(table + ": " + count));
        System.out.println();
        System.out.println("Running it...");
        System.out.println();

        var start = System.currentTimeMillis();

        var results = runTests(dataSource, testCase);

        var duration = Duration.ofMillis(System.currentTimeMillis() - start);

        printDelimiter();
        System.out.println("Test case finished! It had queries:");
        testCase.uniqueAndSortedQueryGroups().forEach(System.out::println);
        var tablesCountAfterTest = tablesCount(dataSource, testCase);
        System.out.println();
        System.out.println("Tables count after test...");
        tablesCountAfterTest.forEach((table, count) -> System.out.println(table + ": " + count));
        System.out.println();
        System.out.println("Some stats...");
        System.out.println();

        var sortedResults = results.stream().sorted(Comparator.comparing(QueryTestResult::time)).toList();
        printStats(testCase, sortedResults, duration);

        printDelimiter();
    }

    static int envIntValueOrDefault(String key, int defaultValue) {
        return Integer.parseInt(envValueOrDefault(key, String.valueOf(defaultValue)));
    }

    static int envIntValueOrThrow(String key) {
        return Integer.parseInt(envValueOrThrow(key));
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
        config.setJdbcUrl("jdbc:sqlite:" + DB_PATH);
        config.setMinimumIdle(envIntValueOrDefault("DATA_SOURCE_POOL_SIZE", 10));
        config.setMaximumPoolSize(envIntValueOrDefault("DATA_SOURCE_POOL_SIZE", 10));
        config.setConnectionTimeout(envIntValueOrDefault("DATA_SOURCE_CONNECTION_TIMEOUT", 3000));
        return new HikariDataSource(config);
    }

    static void runInitQueries(DataSource dataSource) {
        for (var q : SCHEMA.split(";")) {
            if (!q.isBlank()) {
                executeQuery(dataSource, q);
            }
        }
        executeQuery(dataSource, "PRAGMA journal_mode=" + JOURNAL_MODE);
        executeQuery(dataSource, "PRAGMA busy_timeout=5000");
        if (!BEFORE_TESTS_QUERY.isEmpty()) {
            executeQuery(dataSource, BEFORE_TESTS_QUERY);
        }
    }

    static TestCase testCase(DataSource dataSource) {
        var minMaxId = minMaxIdsFromDb(dataSource);
        var minId = minMaxId.getFirst();
        var maxId = minMaxId.getLast();

        var existingNames = existingNamesFromDb(dataSource, 5000);
        var existingEmails = existingEmailsFromDb(dataSource, 5000);

        return switch (TEST_CASE) {
            case BATCH_INSERTS -> batchInsertsTestCase(maxId);
            case WRITES_100 -> writes100TestCase(minId, maxId);
            case READS_100 -> reads100TestCase(minId, maxId, existingNames, existingEmails);
            case WRITES_50_READS_50 -> writesReadsTestCase(minId, maxId, existingNames, existingEmails, 1, 1);
            case WRITES_10_READS_90 -> writesReadsTestCase(minId, maxId, existingNames, existingEmails, 1, 9);
        };
    }

    static List<Long> minMaxIdsFromDb(DataSource dataSource) {
        return executeQuery(dataSource, "SELECT MIN(id), MAX(id) FROM " + TABLE, r -> {
            if (r.next()) {
                return List.of(r.getLong(1), r.getLong(2));
            }
            return List.of(0L, 0L);
        });
    }

    static List<String> existingNamesFromDb(DataSource dataSource, int limit) {
        return executeQuery(dataSource, "SELECT DISTINCT name FROM %s LIMIT %d".formatted(TABLE, limit), r -> {
            var names = new ArrayList<String>();
            while (r.next()) {
                names.add(r.getString(1));
            }
            return names;
        });
    }

    static List<String> existingEmailsFromDb(DataSource dataSource, int limit) {
        return executeQuery(dataSource, "SELECT DISTINCT email FROM %s LIMIT %d".formatted(TABLE, limit), r -> {
            var emails = new ArrayList<String>();
            while (r.next()) {
                emails.add(r.getString(1));
            }
            return emails;
        });
    }

    static TestCase batchInsertsTestCase(long lastId) {
        return new TestCase(
            envQueriesToExecute(),
            envQueriesRate(),
            batchInsertQueryGroup(envIntValueOrThrow("QUERIES_BATCH_SIZE"),
                new AtomicLong(lastId + 1)));
    }

    static QueryGroup batchInsertQueryGroup(int records, AtomicLong nextId) {
        return new QueryGroup("batch-insert", TABLE, () -> insertQuery(records, nextId));
    }

    static QueryGroup insertQueryGroup(AtomicLong nextId) {
        return new QueryGroup("insert", TABLE, () -> insertQuery(1, nextId));
    }

    static String insertQuery(int records, AtomicLong nextId) {
        return "INSERT INTO %s (id, email, name, description, created_at, version) VALUES ".formatted(TABLE)
               + Stream.generate(() -> {
                var id = nextId.getAndIncrement();
                var email = randomName() + "@email.com";
                var name = randomName();
                var description = RANDOM.nextBoolean() ? randomString(50, 500) : null;
                var createdAt = randomTimestamp().getEpochSecond();
                var version = 1;
                return "(%d, '%s', '%s', '%s', %s, %d)"
                    .formatted(id, email, name, description, createdAt, version);
            }
        ).limit(records).collect(Collectors.joining(",\n"));
    }

    static int envQueriesToExecute() {
        return envIntValueOrThrow("QUERIES_TO_EXECUTE");
    }

    static int envQueriesRate() {
        return envIntValueOrThrow("QUERIES_RATE");
    }

    static TestCase reads100TestCase(long minId, long maxId,
                                     List<String> existingNames,
                                     List<String> existingEmails) {
        return new TestCase(envQueriesToExecute(), envQueriesRate(),
            List.of(selectByIdQueryGroup(minId, maxId),
                selectByNameQueryGroup(existingNames),
                selectByEmailQueryGroup(existingEmails)));
    }

    static QueryGroup selectByIdQueryGroup(long minId, long maxId) {
        return new QueryGroup("select-by-id", TABLE, () -> selectByIdQuery(minId, maxId));
    }

    static String selectByIdQuery(long minId, long maxId) {
        return "SELECT * FROM %s WHERE id = %d".formatted(TABLE, minId + RANDOM.nextLong(maxId));
    }

    static QueryGroup selectByNameQueryGroup(List<String> existingNames) {
        return new QueryGroup("select-by-name", TABLE, () -> selectByNameQuery(existingNames));
    }

    static String selectByNameQuery(List<String> names) {
        return "SELECT * FROM %s WHERE name = '%s'".formatted(TABLE, randomChoice(names));
    }

    static QueryGroup selectByEmailQueryGroup(List<String> existingEmails) {
        return new QueryGroup("select-by-email", TABLE, () -> selectByEmailQuery(existingEmails));
    }

    static String selectByEmailQuery(List<String> emails) {
        return "SELECT * FROM %s WHERE email = '%s'".formatted(TABLE, randomChoice(emails));
    }

    static TestCase writes100TestCase(long minId, long maxId) {
        var nextId = new AtomicLong(maxId + 1);
        return new TestCase(envQueriesToExecute(), envQueriesRate(),
            List.of(insertQueryGroup(nextId),
                updateByIdQueryGroup(minId, maxId),
                deleteByIdQueryGroup(minId, maxId)));
    }

    static TestCase writesReadsTestCase(long minId, long maxId,
                                        List<String> existingNames,
                                        List<String> existingEmails,
                                        int writesRatio,
                                        int readsRatio) {
        var nextId = new AtomicLong(maxId + 1);
        var queryGroups = Stream.of(
            Stream.generate(() -> insertQueryGroup(nextId))
                .limit(writesRatio),
            Stream.generate(() -> updateByIdQueryGroup(minId, maxId))
                .limit(writesRatio),
            Stream.generate(() -> deleteByIdQueryGroup(minId, maxId))
                .limit(writesRatio),
            Stream.generate(() -> selectByIdQueryGroup(minId, maxId))
                .limit(readsRatio),
            Stream.generate(() -> selectByNameQueryGroup(existingNames))
                .limit(readsRatio),
            Stream.generate(() -> selectByEmailQueryGroup(existingEmails))
                .limit(readsRatio)
        ).flatMap(Function.identity()).toList();

        return new TestCase(envQueriesToExecute(), envQueriesRate(), queryGroups);
    }

    static QueryGroup updateByIdQueryGroup(long minId, long maxId) {
        return new QueryGroup("update-various-fields-by-id", TABLE, () -> updateByIdQuery(minId, maxId));
    }

    static String updateByIdQuery(long minId, long maxId) {
        var newName = randomName();
        var newEmail = randomName() + "@email.com";
        var newVersion = 1 + RANDOM.nextInt(10_000);

        var id = minId + RANDOM.nextLong(maxId);

        return """
            UPDATE %s
            SET name = '%s',
                email = '%s',
                version = %d
            WHERE id = %d
            """.formatted(TABLE, newName, newEmail, newVersion, id);
    }

    static QueryGroup deleteByIdQueryGroup(long minId, long maxId) {
        return new QueryGroup("delete-by-id", TABLE, () -> deleteByIdQuery(minId, maxId));
    }

    static String deleteByIdQuery(long minId, long maxId) {
        return "DELETE FROM %s WHERE id = %d".formatted(TABLE, minId + RANDOM.nextLong(maxId));
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

    static Map<String, Integer> tablesCount(DataSource source, TestCase testCase) {
        return testCase.queryGroups.stream()
            .flatMap(q -> q.tables.stream())
            .distinct()
            .collect(Collectors.toMap(Function.identity(), table -> tableCount(source, table)));
    }

    static int tableCount(DataSource source, String table) {
        return executeQuery(source, "SELECT COUNT(*) FROM %s".formatted(table), r -> {
            r.next();
            return r.getInt(1);
        });
    }

    static <T> T executeQuery(DataSource source, String query, ResultSetMapper<T> resultMapper) {
        try (var conn = source.getConnection()) {
            var statement = conn.createStatement();
            var result = statement.execute(query);
            if (result) {
                var resultSet = statement.getResultSet();
                return resultMapper.map(resultSet);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void executeQuery(DataSource source, String query) {
        executeQuery(source, query, r -> null);
    }

    static List<QueryTestResult> runTests(DataSource dataSource, TestCase testCase) throws Exception {
        var resultFutures = new LinkedList<Future<QueryTestResult>>();
        var results = new LinkedList<QueryTestResult>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var i = 0; i < testCase.queriesToExecute; i++) {
                var result = executor.submit(() -> queryTest(dataSource, testCase));
                resultFutures.add(result);

                var issuedQueries = i + 1;
                if (resultFutures.size() >= testCase.queriesRate && issuedQueries < testCase.queriesToExecute) {
                    System.out.printf("%s, %d/%d queries were issued, waiting 1s before sending next query batch...%n",
                        LocalDateTime.now(), issuedQueries, testCase.queriesToExecute());
                    Thread.sleep(1000);
                    results.addAll(getFutureResults(resultFutures));
                    resultFutures.clear();
                }
            }

            if (!resultFutures.isEmpty()) {
                results.addAll(getFutureResults(resultFutures));
                resultFutures.clear();
            }
        }

        return results;
    }

    static QueryTestResult queryTest(DataSource dataSource, TestCase testCase) {
        var group = randomChoice(testCase.queryGroups);
        var query = group.query();
        return new QueryTestResult(group.id, executeTimedQuery(dataSource, query));
    }

    static long executeTimedQuery(DataSource dataSource, String query) {
        var start = System.nanoTime();

        try (var conn = dataSource.getConnection()) {
            var statement = conn.createStatement();
            var result = statement.execute(query);

            if (result) {
                var resultSet = statement.getResultSet();
                while (resultSet.next()) {
                    // consume all results to make select measures objective
                }
            }

            return System.nanoTime() - start;
        } catch (Exception e) {
            var truncatedQuery = query.length() > 100 ? query.substring(0, 100) + "..." : query;
            System.out.println("Fail to execute query: " + truncatedQuery);
            e.printStackTrace();
            return 0;
        }
    }

    static <T> T randomChoice(List<T> elements) {
        var idx = RANDOM.nextInt(elements.size());
        return elements.get(idx);
    }

    static List<QueryTestResult> getFutureResults(List<Future<QueryTestResult>> futureResults) {
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

    static void printStats(TestCase testCase, List<QueryTestResult> sortedTestResults, Duration duration) {
        var sortedTimeResults = sortedTestResults.stream().map(QueryTestResult::time).toList();

        System.out.println("Test duration: " + duration);
        System.out.println("Executed queries: " + sortedTimeResults.size());

        System.out.printf("Wanted queries rate: %d/s%n", testCase.queriesRate);
        System.out.printf("Actual queries rate: %d/s%n", actualQueriesRate(sortedTimeResults.size(), duration));

        System.out.println();

        printStats(sortedTimeResults, true);

        System.out.println();
        var sortedQueryGroupKeys = testCase.uniqueAndSortedQueryGroups().stream().map(q -> q.id).toList();
        var groupedResults = sortedTestResults.stream()
            .collect(Collectors.groupingBy(QueryTestResult::queryId));
        System.out.println("Queries % share:");
        sortedQueryGroupKeys.forEach(qid -> {
            var results = groupedResults.get(qid);
            System.out.println(qid + ": " + formattedPercentage(results.size(), sortedTestResults.size()));
        });
        printDelimiter();

        System.out.println("Queries Stats...");
        System.out.println();
        sortedQueryGroupKeys.forEach(qid -> {
            System.out.println(qid + ": ");
            printStats(groupedResults.get(qid).stream().map(QueryTestResult::time).toList(), false);
            System.out.println();
        });
    }

    static long actualQueriesRate(int queries, Duration duration) {
        var durationWithNanos = duration.getSeconds() + (duration.getNano() / 1_000_000_000.0);
        return Math.round(queries / durationWithNanos);
    }

    static void printStats(List<Long> sortedResults, boolean detailed) {
        var min = sortedResults.getFirst();
        var max = sortedResults.getLast();

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
        if (detailed) {
            System.out.println();
        }
        if (detailed) {
            System.out.println("Percentile 50 (Median): " + formattedMillis(percentile50));
            System.out.println("Percentile 75: " + formattedMillis(percentile75));
        }
        System.out.println("Percentile 90: " + formattedMillis(percentile90));
        if (detailed) {
            System.out.println("Percentile 95: " + formattedMillis(percentile95));
        }
        System.out.println("Percentile 99: " + formattedMillis(percentile99));
        if (detailed) {
            System.out.println("Percentile 99.9: " + formattedMillis(percentile999));
        }
    }

    static String formattedMillis(double nanos) {
        return (Math.round(nanos) / 1_000_000.0) + " ms";
    }

    static String formattedPercentage(int number, int all) {
        return String.valueOf(Math.round(number * 10000.0 / all) / 100.0);
    }

    static double percentile(List<Long> data, double percentile) {
        if (data.isEmpty()) {
            throw new RuntimeException("No percentile for empty data");
        }

        if (percentile >= 100) {
            throw new RuntimeException("Valid percentile is less than 100");
        }

        if (percentile <= 1) {
            return data.getFirst();
        }

        var index = (data.size() * percentile) / 100;

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
        BATCH_INSERTS,
        WRITES_100,
        READS_100,
        WRITES_50_READS_50,
        WRITES_10_READS_90
    }

    interface ResultSetMapper<T> {
        T map(ResultSet result) throws Exception;
    }

    record TestCase(int queriesToExecute,
                    int queriesRate,
                    List<QueryGroup> queryGroups) {

        TestCase(int queriesToExecute, int maxRate, QueryGroup queryGroup) {
            this(queriesToExecute, maxRate, List.of(queryGroup));
        }

        TestCase {
            if (queryGroups.isEmpty()) {
                throw new IllegalArgumentException("TestCase must have at least one QueryGroup");
            }
        }

        List<QueryGroup> uniqueAndSortedQueryGroups() {
            var queryGroupsById = queryGroups.stream().collect(Collectors.toMap(q -> q.id, q -> q, (p, n) -> n));
            return queryGroupsById.values()
                .stream()
                .sorted(QueryGroup::compareByQueryType)
                .toList();
        }

        @Override
        public String toString() {
            return "TestCase[queriesToExecute=%d, queriesRate=%d, queryGroups=%s]"
                .formatted(queriesToExecute, queriesRate, uniqueAndSortedQueryGroups());
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

        int compareByQueryType(QueryGroup other) {
            var aId = id.toLowerCase();
            var bId = other.id.toLowerCase();
            if (aId.contains("select") && !bId.contains("select")) {
                return -1;
            }
            if (bId.contains("select") && !aId.contains("select")) {
                return 1;
            }
            if (aId.contains("insert") && !bId.contains("insert")) {
                return -1;
            }
            if (bId.contains("insert") && !aId.contains("insert")) {
                return 1;
            }
            if (aId.contains("update") && !bId.contains("update")) {
                return -1;
            }
            if (bId.contains("update") && !aId.contains("update")) {
                return 1;
            }
            return aId.compareTo(bId);
        }

        @Override
        public String toString() {
            return "QueryGroup[id=%s, tables=%s]".formatted(id, tables);
        }
    }

    record QueryTestResult(String queryId, long time) {
    }
}
