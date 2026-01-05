import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

static final Random RANDOM = new SecureRandom();

static final DbType DB_TYPE = dbTypeFromUrl();

static final String USER_TABLE = safeTableName("user");
static final String ORDER_TABLE = safeTableName("order");
static final String ITEM_TABLE = safeTableName("item");
static final String ORDER_ITEM_TABLE = safeTableName("order_item");

static final String UPPER_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
static final String RANDOM_STRING_ALPHABET = UPPER_ALPHABET + UPPER_ALPHABET.toLowerCase() + "0123456789";

static final TestCase TEST_CASE = testCaseFromEnv();

static final int CONNECTION_POOL_SIZE = envIntValueOrDefault("DATA_SOURCE_CONNECTION_POOL_SIZE", 25);

static final AtomicLong ADDITIONAL_QUERY_DATA_DURATION = new AtomicLong(0);
static Connection ADDITIONAL_QUERY_DATA_CONNECTION;

void main(String[] $) throws Exception {
    IO.println("Starting DB Performance Tests, connecting to %s data source with a pool of %d connections...".formatted(DB_TYPE, CONNECTION_POOL_SIZE));
    IO.println();

    var dataSource = dataSource();

    IO.println();
    IO.println("%s data source connected, about to run %s test case.".formatted(DB_TYPE, TEST_CASE));

    var testCaseSpec = testCaseSpec(dataSource);

    IO.println("The following test case specification will be executed: %s".formatted(testCaseSpec));

    var tablesCountBeforeTest = tablesCount(dataSource, testCaseSpec);
    IO.println();
    IO.println("Tables count before test:");
    tablesCountBeforeTest.forEach((table, count) -> IO.println("%s: %s".formatted(table, count)));
    IO.println();

    var resultFutures = new LinkedList<Future<QueryTestResult>>();
    var results = new LinkedList<QueryTestResult>();

    var start = System.currentTimeMillis();

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        for (var i = 0; i < testCaseSpec.queriesToExecute(); i++) {
            var result = executor.submit(() -> queryTest(dataSource, testCaseSpec.queryGroups()));
            resultFutures.add(result);

            var issuedQueries = i + 1;
            if (issuedQueries % testCaseSpec.queriesRate() == 0 && issuedQueries < testCaseSpec.queriesToExecute()) {
                IO.println("%s, %d/%d queries were issued, waiting 1s before sending next query batch..."
                        .formatted(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS), issuedQueries, testCaseSpec.queriesToExecute()));
                Thread.sleep(1000);
            }

            if (resultFutures.size() >= testCaseSpec.queriesRate()) {
                results.addAll(getFutureResults(resultFutures));
                resultFutures.clear();
            }
        }

        if (!resultFutures.isEmpty()) {
            results.addAll(getFutureResults(resultFutures));
            resultFutures.clear();
        }
    }

    var duration = Duration.ofMillis(System.currentTimeMillis() - start);

    printDelimiter();

    IO.println("Test case %s with %s data source finished! It had queries: %s".formatted(TEST_CASE, DB_TYPE, testCaseSpec.queryGroups()));
    var tablesCountAfterTest = tablesCount(dataSource, testCaseSpec);
    IO.println();
    IO.println("Tables count after test:");
    tablesCountAfterTest.forEach((table, count) -> IO.println("%s: %s".formatted(table, count)));
    IO.println();
    IO.println("Some stats...");
    IO.println();

    var sortedResults = results.stream().map(QueryTestResult::testDuration).sorted().toList();

    var executedQueriesByGroupId = new LinkedHashMap<String, Integer>();
    testCaseSpec.queryGroups().forEach(g -> executedQueriesByGroupId.putIfAbsent(g.id, 0));
    results.forEach(r -> executedQueriesByGroupId.merge(r.groupId(), 1, Integer::sum));

    printStats(sortedResults, executedQueriesByGroupId, duration, testCaseSpec.queriesRate());
}

static DbType dbTypeFromUrl() {
    var jdbcUrl = envValueOrThrow("DATA_SOURCE_URL");
    if (jdbcUrl.contains("mysql")) {
        return DbType.MYSQL;
    }
    if (jdbcUrl.contains("postgresql")) {
        return DbType.POSTGRESQL;
    }
    throw new IllegalArgumentException("Cannot resolve db type from %s url".formatted(jdbcUrl));
}

static String safeTableName(String table) {
    return switch (DB_TYPE) {
        case MYSQL -> "`%s`".formatted(table);
        case POSTGRESQL -> "\"%s\"".formatted(table);
    };
}

static TestCase testCaseFromEnv() {
    var testCase = envValueOrThrow("TEST_CASE");
    try {
        return TestCase.valueOf(testCase.toUpperCase());
    } catch (Exception e) {
        throw new IllegalArgumentException("%s is not a supported test case. Supported are: %s".formatted(testCase, Arrays.toString(TestCase.values())));
    }
}

static int envIntValueOrDefault(String key, int defaultValue) {
    return Integer.parseInt(envValueOrDefault(key, String.valueOf(defaultValue)));
}

static String envValueOrDefault(String key, String defaultValue) {
    return System.getenv().getOrDefault(key, defaultValue);
}

static String envValueOrThrow(String key) {
    return Optional.ofNullable(System.getenv().get(key))
            .orElseThrow(() -> new IllegalArgumentException("%s env variable is required but was not supplied!".formatted(key)));
}

static void printDelimiter() {
    IO.println();
    IO.println("...");
    IO.println();
}

static DataSource dataSource() {
    var config = new HikariConfig();
    var jdbcUrl = envValueOrThrow("DATA_SOURCE_URL");

    if (DB_TYPE == DbType.MYSQL) {
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
    } else if (DB_TYPE == DbType.POSTGRESQL) {
        config.setDriverClassName("org.postgresql.Driver");
    }

    config.setJdbcUrl(jdbcUrl);
    config.setUsername(envValueOrThrow("DATA_SOURCE_USERNAME"));
    config.setPassword(envValueOrThrow("DATA_SOURCE_PASSWORD"));
    config.setMinimumIdle(CONNECTION_POOL_SIZE);
    config.setMaximumPoolSize(CONNECTION_POOL_SIZE);
    config.setPoolName(DB_TYPE.name());

    return new HikariDataSource(config);
}

static TestCaseSpec testCaseSpec(DataSource dataSource) {
    return switch (TEST_CASE) {
        case INSERT_USERS -> insertUsersSpec();
        case INSERT_ITEMS_IN_BATCHES -> insertItemsInBatchesSpec();
        case INSERT_ORDERS_IN_BATCHES -> insertOrdersInBatchesSpec(dataSource);
        case INSERT_ORDER_ITEMS_IN_BATCHES -> insertOrderItemsInBatchesSpec(dataSource);
        case SELECT_USERS_BY_ID -> selectUsersByIdSpec(dataSource);
        case SELECT_USERS_BY_EMAIL -> selectUsersByEmailSpec(dataSource);
        case SELECT_SORTED_BY_ID_USER_PAGES -> selectSortedByIdUserPagesSpec(dataSource);
        case SELECT_ORDERS_JOINED_WITH_USERS -> selectOrdersJoinedWithUsersSpec(dataSource);
        case SELECT_ORDERS_JOINED_WITH_ITEMS -> selectOrdersJoinedWithItemsSpec(dataSource);
        case SELECT_USERS_WITH_ORDERS_STATS_BY_ID -> selectUsersWithOrderStatsByIdSpec(dataSource);
        case UPDATE_USER_EMAILS_BY_ID -> updateUserEmailsByIdSpec(dataSource);
        case UPDATE_USER_UPDATED_ATS_BY_ID -> updateUserUpdatedAtsByIdSpec(dataSource);
        case UPDATE_USER_MULTIPLE_COLUMNS_BY_ID -> updateUserMultipleColumnsByIdSpec(dataSource);
        case DELETE_ORDERS_BY_ID -> deleteOrdersByIdSpec(dataSource);
        case DELETE_ORDERS_IN_BATCHES_BY_ID -> deleteOrdersInBatchesByIdSpec(dataSource);
        case INSERT_USERS_AND_ORDERS_WITH_ITEMS_IN_TRANSACTIONS ->
                insertUsersAndOrdersWithItemsInTransactionsSpec(dataSource);
        case INSERT_UPDATE_DELETE_AND_SELECT_USERS_BY_ID -> insertUpdateDeleteAndSelectUsersByIdSpec(dataSource);
    };
}

static TestCaseSpec insertUsersSpec() {
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(500_000),
            envQueriesRateOrDefault(10_000),
            new QueryGroup("insert-users", USER_TABLE, () -> insertUserQuery()));
}

static TestCaseSpec insertItemsInBatchesSpec() {
    var batchSize = 100;
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(5000),
            envQueriesRateOrDefault(500),
            new QueryGroup("insert-items-in-batches-of-%d".formatted(batchSize), ITEM_TABLE,
                    () -> insertItemQuery(batchSize)));
}

static TestCaseSpec insertOrdersInBatchesSpec(DataSource dataSource) {
    var batchSize = 100;
    var nextUserId = nextIdSupplier(dataSource, USER_TABLE);
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(20_000),
            envQueriesRateOrDefault(2000),
            new QueryGroup("insert-orders-in-batches-of-%d".formatted(batchSize), List.of(ORDER_TABLE),
                    () -> insertOrderQuery(batchSize, nextUserId)));
}

static Supplier<Long> nextIdSupplier(DataSource dataSource, String table) {
    return nextValueSupplier(dataSource, table, "id", r -> r.getLong(1));
}

static <T> Supplier<T> nextValueSupplier(DataSource dataSource, String table, String field,
                                         QueryResultMapper<T> valueMapper) {
    // shared connection not to affect the main test case queries
    if (ADDITIONAL_QUERY_DATA_CONNECTION == null) {
        try {
            ADDITIONAL_QUERY_DATA_CONNECTION = dataSource.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    return new Supplier<>() {
        final AtomicInteger usageCount = new AtomicInteger(0);
        final double maxValuesUsage = 0.5;
        private final Lock lock = new ReentrantLock();
        volatile List<T> nextValues;

        @Override
        public T get() {
            try {
                lock.lock();
                if (nextValues == null || usageCount.getAndIncrement() >= (maxValuesUsage * nextValues.size())) {
                    var overheadStart = System.nanoTime();

                    nextValues = nextValuesFromDb();
                    usageCount.set(1);

                    var overheadDuration = System.nanoTime() - overheadStart;
                    ADDITIONAL_QUERY_DATA_DURATION.addAndGet(overheadDuration);
                }
            } finally {
                lock.unlock();
            }
            return randomChoice(nextValues);
        }

        private List<T> nextValuesFromDb() {
            final int tableSize = tableCount(ADDITIONAL_QUERY_DATA_CONNECTION, table);
            final int batchSize = Math.min(100_000, tableSize);
            var nextOffset = tableSize > batchSize ? randomNumber(0, tableSize - batchSize) : 0;
            return executeQuery(ADDITIONAL_QUERY_DATA_CONNECTION,
                    "SELECT %s FROM %s ORDER BY %s LIMIT %d OFFSET %d".formatted(field, table, field, batchSize, nextOffset),
                    r -> {
                        var values = new ArrayList<T>();
                        while (r.next()) {
                            values.add(valueMapper.map(r));
                        }
                        return values;
                    });
        }
    };
}

static TestCaseSpec insertOrderItemsInBatchesSpec(DataSource dataSource) {
    var batchSize = 1000;
    var nextOrderId = nextIdSupplier(dataSource, ORDER_TABLE);
    var nextItemId = nextIdSupplier(dataSource, ITEM_TABLE);
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(4000),
            envQueriesRateOrDefault(400),
            new QueryGroup("insert-order-items-in-batches-of-%d".formatted(batchSize), List.of(ORDER_ITEM_TABLE),
                    () -> insertOrderItemQuery(batchSize, nextOrderId, nextItemId)));
}

static TestCaseSpec selectUsersByIdSpec(DataSource dataSource) {
    var nextUserId = nextIdSupplier(dataSource, USER_TABLE);
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(500_000),
            envQueriesRateOrDefault(50_000),
            new QueryGroup("select-users-by-id", USER_TABLE,
                    () -> "SELECT * FROM %s WHERE id = %d".formatted(USER_TABLE, nextUserId.get())));
}

static TestCaseSpec selectUsersByEmailSpec(DataSource dataSource) {
    var nextEmail = nextValueSupplier(dataSource, USER_TABLE, "email", r -> r.getString(1));
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(500_000),
            envQueriesRateOrDefault(50_000),
            new QueryGroup("select-users-by-email", USER_TABLE,
                    () -> "SELECT * FROM %s WHERE email = '%s'".formatted(USER_TABLE, nextEmail.get())));
}

static TestCaseSpec selectSortedByIdUserPagesSpec(DataSource dataSource) {
    var usersCount = tableCount(dataSource, USER_TABLE);
    // reasonable offset not to slow down it too much
    var maxOffset = Math.clamp(usersCount, 0, 10_000);
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(50_000),
            envQueriesRateOrDefault(5000),
            new QueryGroup("select-sorted-by-id-user-pages", USER_TABLE, () -> {
                var limit = randomNumber(10, 100);
                var offset = randomNumber(0, maxOffset);
                return "SELECT * FROM %s ORDER BY id LIMIT %d OFFSET %d"
                        .formatted(USER_TABLE, limit, offset);
            }));
}

static TestCaseSpec selectOrdersJoinedWithUsersSpec(DataSource dataSource) {
    var nextOrderId = nextIdSupplier(dataSource, ORDER_TABLE);
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(350_000),
            envQueriesRateOrDefault(35_000),
            new QueryGroup("select-orders-joined-with-users",
                    List.of(ORDER_TABLE, USER_TABLE),
                    () -> "SELECT * FROM %s AS o INNER JOIN %s AS u ON o.user_id = u.id WHERE o.id = %s"
                            .formatted(ORDER_TABLE, USER_TABLE, nextOrderId.get())));
}

static TestCaseSpec selectOrdersJoinedWithItemsSpec(DataSource dataSource) {
    var nextOrderId = nextIdSupplier(dataSource, ORDER_TABLE);
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(300_000),
            envQueriesRateOrDefault(30_000),
            new QueryGroup("select-orders-joined-with-items",
                    List.of(ORDER_TABLE, ORDER_ITEM_TABLE, ITEM_TABLE),
                    () -> """
                            SELECT o.*, i.*
                            FROM %s AS o
                            INNER JOIN %s AS oi ON o.id = oi.order_id
                            INNER JOIN %s AS i ON oi.item_id = i.id
                            WHERE o.id = %d"""
                            .strip()
                            .formatted(ORDER_TABLE, ORDER_ITEM_TABLE, ITEM_TABLE,
                                    nextOrderId.get())));
}

static TestCaseSpec selectUsersWithOrderStatsByIdSpec(DataSource dataSource) {
    var nextUserId = nextIdSupplier(dataSource, USER_TABLE);
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(400_000),
            envQueriesRateOrDefault(40_000),
            new QueryGroup("select-users-with-orders-stats-by-id",
                    List.of(USER_TABLE, ORDER_TABLE),
                    () -> """
                            SELECT u.id AS user_id, COUNT(*) AS orders,
                            MIN(o.created_at) AS oldest_order_created_at,
                            MAX(o.created_at) AS latest_order_created_at
                            FROM %s AS u INNER JOIN %s AS o on u.id = o.user_id
                            WHERE u.id = %s
                            GROUP BY u.id
                            """.formatted(USER_TABLE, ORDER_TABLE, nextUserId.get())));
}

static TestCaseSpec updateUserEmailsByIdSpec(DataSource dataSource) {
    var nextUserId = nextIdSupplier(dataSource, USER_TABLE);
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(50_000),
            envQueriesRateOrDefault(5_000),
            new QueryGroup("update-user-emails-by-id",
                    List.of(USER_TABLE),
                    () -> """
                            UPDATE %s
                            SET email = '%s'
                            WHERE id = %d
                            """
                            .formatted(USER_TABLE, randomEmail(), nextUserId.get())
                            .strip()));
}

static TestCaseSpec updateUserUpdatedAtsByIdSpec(DataSource dataSource) {
    var nextUserId = nextIdSupplier(dataSource, USER_TABLE);
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(50_000),
            envQueriesRateOrDefault(5_000),
            new QueryGroup("update-user-updated-ats-by-id",
                    List.of(USER_TABLE),
                    () -> """
                            UPDATE %s
                            SET updated_at = %s
                            WHERE id = %d
                            """
                            .formatted(USER_TABLE,
                                    RANDOM.nextBoolean() ? "NULL" : "'%s'".formatted(timestampToString(randomTimestamp())),
                                    nextUserId.get())
                            .strip()));
}


static TestCaseSpec updateUserMultipleColumnsByIdSpec(DataSource dataSource) {
    var nextUserId = nextIdSupplier(dataSource, USER_TABLE);
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(50_000),
            envQueriesRateOrDefault(5_000),
            new QueryGroup("update-user-multiple-columns-by-id",
                    List.of(USER_TABLE),
                    () -> """
                            UPDATE %s
                            SET email = '%s',
                                updated_at = %s
                            WHERE id = %d
                            """
                            .formatted(USER_TABLE,
                                    randomEmail(),
                                    RANDOM.nextBoolean() ? "NULL" : "'%s'".formatted(timestampToString(randomTimestamp())),
                                    nextUserId.get())
                            .strip()));
}

static TestCaseSpec deleteOrdersByIdSpec(DataSource dataSource) {
    var nextOrderId = nextIdSupplier(dataSource, ORDER_TABLE);
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(100_000),
            envQueriesRateOrDefault(10_000),
            new QueryGroup("delete-orders-by-id",
                    List.of(ORDER_TABLE),
                    () -> """
                            DELETE FROM %s
                            WHERE id = %d
                            """
                            .formatted(ORDER_TABLE, nextOrderId.get())
                            .strip()));
}

static TestCaseSpec deleteOrdersInBatchesByIdSpec(DataSource dataSource) {
    var nextOrderId = nextIdSupplier(dataSource, ORDER_TABLE);
    var batchSize = 100;
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(10_000),
            envQueriesRateOrDefault(1000),
            new QueryGroup("delete-orders-in-batches-of-%d-by-id".formatted(batchSize),
                    List.of(ORDER_TABLE),
                    () -> {
                        var idsToDelete = Stream.generate(() -> String.valueOf(nextOrderId.get()))
                                .limit(batchSize)
                                .collect(Collectors.joining(","));
                        return """
                                DELETE FROM %s
                                WHERE id IN (%s)
                                """
                                .formatted(ORDER_TABLE, idsToDelete)
                                .strip();
                    }));
}

static TestCaseSpec insertUsersAndOrdersWithItemsInTransactionsSpec(DataSource dataSource) {
    var nextUserId = nextIdSupplier(dataSource, USER_TABLE);
    var nextOrderId = nextIdSupplier(dataSource, ORDER_TABLE);
    var nextItemId = nextIdSupplier(dataSource, ITEM_TABLE);
    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(25_000),
            envQueriesRateOrDefault(2500),
            new QueryGroup("insert-users-and-orders-with-items-in-transaction",
                    List.of(USER_TABLE, ORDER_TABLE, ORDER_ITEM_TABLE),
                    () -> """
                            %s;
                            %s;
                            %s;
                            """
                            .formatted(insertUserQuery(),
                                    insertOrderQuery(1, nextUserId),
                                    insertOrderItemQuery(2, nextOrderId, nextItemId))
                            .strip()));
}

static TestCaseSpec insertUpdateDeleteAndSelectUsersByIdSpec(DataSource dataSource) {
    var nextUserId = nextIdSupplier(dataSource, USER_TABLE);

    var selectUserByIdQueryGroup = new QueryGroup("select-users-by-id", USER_TABLE,
            () -> "SELECT * FROM %s WHERE id = %d".formatted(USER_TABLE, nextUserId.get()));

    return new TestCaseSpec(
            envQueriesToExecuteOrDefault(75_000),
            envQueriesRateOrDefault(7500),
            List.of(new QueryGroup("insert-users", USER_TABLE, () -> insertUserQuery()),
                    new QueryGroup("update-user-emails-by-id", USER_TABLE, () -> updateUserEmailByIdQuery(nextUserId)),
                    new QueryGroup("delete-users-by-id", USER_TABLE,
                            () -> "DELETE FROM %s WHERE id = %d".formatted(USER_TABLE, nextUserId.get())),
                    selectUserByIdQueryGroup, selectUserByIdQueryGroup, selectUserByIdQueryGroup));
}

static int envQueriesToExecuteOrDefault(int defaultValue) {
    return envIntValueOrDefault("QUERIES_TO_EXECUTE", defaultValue);
}

static int envQueriesRateOrDefault(int defaultValue) {
    return envIntValueOrDefault("QUERIES_RATE", defaultValue);
}

static String insertUserQuery() {
    return "INSERT INTO %s (email, created_at) VALUES ('%s', '%s')"
            .formatted(USER_TABLE, randomEmail(), timestampToString(randomTimestamp()));
}

static String insertItemQuery(int records) {
    return "INSERT INTO %s (name, description, price, created_at, updated_at) VALUES ".formatted(ITEM_TABLE)
            + Stream.generate(() -> {
        var name = randomName();
        var description = RANDOM.nextBoolean() ? randomString(5, 1000) : null;
        var price = new BigDecimal(RANDOM.nextDouble() * 100).setScale(2, RoundingMode.HALF_UP);
        var createdAt = randomTimestamp();
        var updatedAt = RANDOM.nextBoolean() ? createdAt.plusSeconds(RANDOM.nextLong(TimeUnit.DAYS.toSeconds(365))) : null;
        return "('%s', '%s', %s, '%s', %s)".formatted(name, description, price, timestampToString(createdAt),
                updatedAt == null ? "NULL" : "'%s'".formatted(timestampToString(updatedAt)));
    }).limit(records).collect(Collectors.joining(",\n"));
}

static String insertOrderQuery(int records, Supplier<Long> nextUserId) {
    return "INSERT INTO %s (user_id,  created_at, updated_at) VALUES ".formatted(ORDER_TABLE)
            + Stream.generate(() -> {
        var userId = nextUserId.get();
        var createdAt = randomTimestamp();
        var updatedAt = RANDOM.nextBoolean() ? createdAt.plusSeconds(RANDOM.nextLong(TimeUnit.DAYS.toSeconds(365))) : null;
        return "(%d, '%s', %s)".formatted(userId, timestampToString(createdAt),
                updatedAt == null ? "NULL" : "'%s'".formatted(timestampToString(updatedAt)));
    }).limit(records).collect(Collectors.joining(",\n"));
}

static String insertOrderItemQuery(int records, Supplier<Long> nextOrderId, Supplier<Long> nextItemId) {
    return "INSERT INTO %s (order_id, item_id) VALUES ".formatted(ORDER_ITEM_TABLE)
            + Stream.generate(() -> "(%d, %d)".formatted(nextOrderId.get(), nextItemId.get()))
            .limit(records).collect(Collectors.joining(",\n"));
}

static String updateUserEmailByIdQuery(Supplier<Long> nextUserId) {
    return """
            UPDATE %s
            SET email = '%s'
            WHERE id = %d
            """
            .formatted(USER_TABLE, randomEmail(), nextUserId.get())
            .strip();
}

static int randomNumber(int min, int max) {
    return min + RANDOM.nextInt(max - min);
}

static <T> T randomChoice(List<T> elements) {
    var idx = RANDOM.nextInt(elements.size());
    return elements.get(idx);
}

static String randomName() {
    return randomString(10, 50);
}

static String randomEmail() {
    return randomName() + "@email.com";
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

static QueryTestResult queryTest(DataSource dataSource, List<QueryGroup> queryGroups) {
    // make queries more uniform
    randomDelay();

    var group = randomChoice(queryGroups);
    long testDuration;

    try (var conn = dataSource.getConnection()) {
        conn.setAutoCommit(false);

        var query = group.query();

        // possible multiple queries to support transactions
        var queries = query.split(";");

        // start time after query rendering as String - it's a function and might introduce a noticeable time overhead
        var start = System.nanoTime();

        for (var q : queries) {
            if (q.isBlank()) {
                continue;
            }

            var statement = conn.createStatement();
            var result = statement.execute(q);

            if (result) {
                var resultSet = statement.getResultSet();
                while (resultSet.next()) {
                    // consuming result to make select measures objective
                }
            }
        }

        conn.commit();

        testDuration = System.nanoTime() - start;
    } catch (Exception e) {
        e.printStackTrace();
        testDuration = 0;
    }

    return new QueryTestResult(group.id, testDuration);
}

static void randomDelay() {
    try {
        var delay = RANDOM.nextInt(1000);
        Thread.sleep(delay);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

static Map<String, Integer> tablesCount(DataSource dataSource, TestCaseSpec testCaseSpec) {
    return testCaseSpec.queryGroups().stream()
            .flatMap(q -> q.tables.stream())
            .distinct()
            .collect(Collectors.toMap(Function.identity(), table -> tableCount(dataSource, table)));
}

static int tableCount(DataSource dataSource, String table) {
    return executeQuery(dataSource, "SELECT COUNT(*) FROM " + table, r -> {
        r.next();
        return r.getInt(1);
    });
}

static int tableCount(Connection connection, String table) {
    return executeQuery(connection, "SELECT COUNT(*) FROM " + table, r -> {
        r.next();
        return r.getInt(1);
    });
}

static <T> T executeQuery(DataSource dataSource, String query, QueryResultMapper<T> mapper) {
    try (var conn = dataSource.getConnection()) {
        return executeQuery(conn, query, mapper);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

static <T> T executeQuery(Connection connection, String query, QueryResultMapper<T> mapper) {
    try {
        var result = connection.createStatement().executeQuery(query);
        return mapper.map(result);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
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

static void printStats(List<Long> sortedResults, Map<String, Integer> executedQueriesByGroupId,
                       Duration duration, int queriesRate) {
    var min = sortedResults.getFirst();
    var max = sortedResults.getLast();

    var queriesDuration = duration.minusNanos(ADDITIONAL_QUERY_DATA_DURATION.get())
            .truncatedTo(ChronoUnit.MILLIS);

    IO.println("Total test duration: " + duration);
    IO.println("Queries duration: " + queriesDuration);
    IO.println();
    IO.println("Executed queries: " + sortedResults.size());
    if (executedQueriesByGroupId.size() > 1) {
        executedQueriesByGroupId.forEach((groupId, queries) -> IO.println("  %s: %d".formatted(groupId, queries)));
    }
    IO.println();
    IO.println("Wanted queries rate: %d/s".formatted(queriesRate));
    IO.println("Actual queries rate: %d/s".formatted(actualQueriesRate(sortedResults.size(), queriesDuration)));
    IO.println();

    var mean = sortedResults.stream().mapToLong(Long::longValue).average().getAsDouble();
    var percentile50 = percentile(sortedResults, 50);
    var percentile75 = percentile(sortedResults, 75);
    var percentile90 = percentile(sortedResults, 90);
    var percentile99 = percentile(sortedResults, 99);
    var percentile999 = percentile(sortedResults, 99.9);

    IO.println("Min: " + formattedMillis(min));
    IO.println("Max: " + formattedMillis(max));
    IO.println("Mean: " + formattedMillis(mean));
    IO.println();
    IO.println("Percentile 50 (Median): " + formattedMillis(percentile50));
    IO.println("Percentile 75: " + formattedMillis(percentile75));
    IO.println("Percentile 90: " + formattedMillis(percentile90));
    IO.println("Percentile 99: " + formattedMillis(percentile99));
    IO.println("Percentile 99.9: " + formattedMillis(percentile999));
}

static long actualQueriesRate(int queries, Duration duration) {
    var durationWithNanos = duration.getSeconds() + (duration.getNano() / 1_000_000_000.0);
    return Math.round(queries / durationWithNanos);
}

static String formattedMillis(double nanos) {
    return (Math.round(nanos / 1000) / 1000.0) + " ms";
}

static double percentile(List<Long> data, double percentile) {
    if (data.isEmpty()) {
        throw new IllegalArgumentException("No percentile for empty data");
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

enum DbType {
    MYSQL, POSTGRESQL
}

enum TestCase {
    INSERT_USERS,
    INSERT_ITEMS_IN_BATCHES,
    INSERT_ORDERS_IN_BATCHES,
    INSERT_ORDER_ITEMS_IN_BATCHES,
    // primary key - should make difference for MySQL
    SELECT_USERS_BY_ID,
    // secondary key - should make difference for MySQL
    SELECT_USERS_BY_EMAIL,
    SELECT_SORTED_BY_ID_USER_PAGES,
    // single join: order -> user, many to one
    SELECT_ORDERS_JOINED_WITH_USERS,
    // double join: order -> order_item -> item, many to many, many to many
    SELECT_ORDERS_JOINED_WITH_ITEMS,
    // single join: user -> order, one to many + a few aggregate functions
    SELECT_USERS_WITH_ORDERS_STATS_BY_ID,
    // indexed column update
    UPDATE_USER_EMAILS_BY_ID,
    // unindexed column update
    UPDATE_USER_UPDATED_ATS_BY_ID,
    // indexed + unindexed column update
    UPDATE_USER_MULTIPLE_COLUMNS_BY_ID,
    DELETE_ORDERS_BY_ID,
    DELETE_ORDERS_IN_BATCHES_BY_ID,
    // transaction with a few write queries
    INSERT_USERS_AND_ORDERS_WITH_ITEMS_IN_TRANSACTIONS,
    // mixed writes and reads case of the same table
    INSERT_UPDATE_DELETE_AND_SELECT_USERS_BY_ID
}

record TestCaseSpec(int queriesToExecute, int queriesRate, List<QueryGroup> queryGroups) {
    TestCaseSpec(int queriesToExecute, int queriesRate, QueryGroup queryGroup) {
        this(queriesToExecute, queriesRate, List.of(queryGroup));
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

interface QueryResultMapper<T> {
    T map(ResultSet resultSet) throws Exception;
}

record QueryTestResult(String groupId, long testDuration) {
}