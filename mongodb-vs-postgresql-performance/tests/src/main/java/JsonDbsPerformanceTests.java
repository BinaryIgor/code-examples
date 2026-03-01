import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.pojo.annotations.BsonId;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

static final String DB_URL = envValueOrThrow("DB_URL");
static final DbType DB_TYPE = dbTypeFromUrl();
static final int CONNECTION_POOL_SIZE = envIntValueOrDefault("DB_CONNECTION_POOL_SIZE", 25);

static final TestCase TEST_CASE = testCaseFromEnv();

static final String ACCOUNTS_COLLECTION = "accounts";
static final String PRODUCTS_COLLECTION = "products";
static final boolean PRODUCTS_WITHOUT_DESCRIPTION = envValueOrDefault("PRODUCTS_WITHOUT_DESCRIPTION", "false").equalsIgnoreCase("true");

static final Random RANDOM = new SecureRandom();
static final String UPPER_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
static final String RANDOM_STRING_ALPHABET = UPPER_ALPHABET + UPPER_ALPHABET.toLowerCase() + "0123456789";

static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

static final int MAX_USER_ID = 1_000_000;
static final List<String> ACCOUNT_TYPES = List.of("FREE", "PLUS", "GOLD", "PLATINUM");
static final List<String> CATEGORIES = List.of(
        "Electronics",
        "Mobile Phones",
        "Smartphones",
        "Laptops",
        "Desktop Computers",
        "Computer Accessories",
        "Wearable Technology",
        "Home Appliances",
        "Kitchen Appliances",
        "Small Appliances",
        "Televisions",
        "Audio Equipment",
        "Headphones",
        "Bluetooth Speakers",
        "Cameras",
        "Photography Accessories",
        "Video Game Consoles",
        "Video Games",
        "Smart Home Devices",
        "Networking Equipment",
        "Furniture",
        "Living Room Furniture",
        "Bedroom Furniture",
        "Office Furniture",
        "Home Decor",
        "Lighting",
        "Bedding",
        "Bath Accessories",
        "Cleaning Supplies",
        "Tools & Hardware",
        "Power Tools",
        "Hand Tools",
        "Garden & Outdoor",
        "Outdoor Furniture",
        "Grills & Barbecues",
        "Sports & Fitness",
        "Exercise Equipment",
        "Clothing",
        "Men's Clothing",
        "Women's Clothing",
        "Shoes",
        "Accessories",
        "Watches",
        "Jewelry",
        "Beauty & Personal Care",
        "Skincare",
        "Hair Care",
        "Health & Wellness",
        "Pet Supplies"
);
static final List<String> TAGS = List.of(
        "new-arrival",
        "best-seller",
        "limited-edition",
        "on-sale",
        "clearance",
        "eco-friendly",
        "energy-efficient",
        "wireless",
        "bluetooth",
        "smart-enabled",
        "voice-control",
        "usb-c",
        "fast-charging",
        "water-resistant",
        "waterproof",
        "lightweight",
        "portable",
        "compact",
        "premium",
        "budget-friendly",
        "high-performance",
        "ergonomic",
        "durable",
        "noise-cancelling",
        "touchscreen",
        "4k",
        "hdr",
        "gaming",
        "professional-grade",
        "home-office",
        "outdoor-use",
        "indoor-use",
        "easy-install",
        "maintenance-free",
        "rechargeable",
        "battery-powered",
        "corded",
        "adjustable",
        "foldable",
        "stackable",
        "modern-design",
        "classic-style",
        "minimalist",
        "kids-friendly",
        "pet-friendly",
        "travel-friendly",
        "multi-purpose",
        "space-saving",
        "customizable"
);
static final String SIZE_VARIATION = "SIZE";
static final String COLOR_VARIATION = "COLOR";
static final List<String> VARIATIONS = List.of(SIZE_VARIATION, COLOR_VARIATION);
static final List<String> SIZE_VARIATIONS = List.of("S", "M", "L", "XL", "XXL", "1", "2", "3", "4", "5");
static final List<String> COLOR_VARIATIONS = List.of("red", "orange", "amber", "yellow",
        "lime", "green", "emerald", "teal", "cyan",
        "sky", "blue", "indigo", "violet", "purple",
        "fuchsia", "pink", "rose", "slate", "gray",
        "zinc", "neutral", "stone");

static long ADDITIONAL_QUERIES_DURATION_MILLIS = 0;

void main(String[] $) throws Exception {
    IO.println("Starting Json DBs Performance Tests, connecting to %s with a pool of %d connections...".formatted(DB_TYPE, CONNECTION_POOL_SIZE));
    IO.println();

    var dbClient = dbClient();

    IO.println();
    IO.println("Connected with %s, about to run %s test case.".formatted(DB_TYPE, TEST_CASE));

    var testCaseSpec = testCaseSpec(dbClient);

    IO.println("The following test case specification will be executed: %s".formatted(testCaseSpec));
    var collectionCountBeforeTest = dbClient.count(testCaseSpec.collection());
    IO.println();
    IO.println("Collection count before test: %d".formatted(collectionCountBeforeTest));
    IO.println();

    var resultFutures = new LinkedList<Future<QueryTestResult>>();
    var results = new LinkedList<QueryTestResult>();

    var start = System.currentTimeMillis();

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        for (var i = 0; i < testCaseSpec.queriesToExecute; i++) {
            var result = executor.submit(() -> queryTest(testCaseSpec.queries()));
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

    IO.println("Test case %s with %s finished! It had queries: %s".formatted(TEST_CASE, DB_TYPE, testCaseSpec.queries()));
    IO.println();
    var collectionCountAfterTest = dbClient.count(testCaseSpec.collection());
    IO.println("Collection count after test: %d".formatted(collectionCountAfterTest));
    IO.println();
    IO.println("Some stats...");
    IO.println();

    var sortedResults = results.stream().map(QueryTestResult::testDuration).sorted().toList();

    var executedQueriesById = new LinkedHashMap<String, Integer>();
    testCaseSpec.queries().forEach(q -> executedQueriesById.putIfAbsent(q.id, 0));
    results.forEach(r -> executedQueriesById.merge(r.queryId(), 1, Integer::sum));

    printStats(sortedResults, executedQueriesById, duration, testCaseSpec.queriesRate());
}

static TestCase testCaseFromEnv() {
    var testCase = envValueOrThrow("TEST_CASE");
    try {
        return TestCase.valueOf(testCase.toUpperCase());
    } catch (Exception e) {
        throw new IllegalArgumentException("%s is not a supported test case. Supported are: %s".formatted(testCase, Arrays.toString(TestCase.values())));
    }
}

static DbType dbTypeFromUrl() {
    if (DB_URL.contains("mongodb")) {
        return DbType.MONGODB;
    }
    if (DB_URL.contains("postgresql")) {
        return DbType.POSTGRESQL;
    }
    throw new IllegalArgumentException("Cannot resolve db type from %s url".formatted(DB_URL));
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

static int envQueriesToExecuteOrDefault(int defaultValue) {
    return envIntValueOrDefault("QUERIES_TO_EXECUTE", defaultValue);
}

static int envQueriesRateOrDefault(int defaultValue) {
    return envIntValueOrDefault("QUERIES_RATE", defaultValue);
}

static void printDelimiter() {
    IO.println();
    IO.println("...");
    IO.println();
}

static DbClient dbClient() {
    return switch (DB_TYPE) {
        case MONGODB -> {
            var connectionString = new ConnectionString(DB_URL);
            var settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .writeConcern(WriteConcern.JOURNALED)
                    .build();
            var client = MongoClients.create(settings);
            var database = client.getDatabase(connectionString.getDatabase());
            yield new MongoDbClient(database);
        }
        case POSTGRESQL -> {
            var config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setUsername(envValueOrThrow("DB_USER"));
            config.setPassword(envValueOrThrow("DB_PASSWORD"));
            config.setMinimumIdle(CONNECTION_POOL_SIZE);
            config.setMaximumPoolSize(CONNECTION_POOL_SIZE);
            config.setPoolName(DB_TYPE.name());

            var dataSource = new HikariDataSource(config);

            yield new PostgresClient(dataSource);
        }
    };
}

static TestCaseSpec<?> testCaseSpec(DbClient dbClient) {
    return switch (TEST_CASE) {
        case INSERT_ACCOUNTS -> insertAccountsSpec(dbClient);
        case INSERT_PRODUCTS -> insertProductsSpec(dbClient);
        case BATCH_INSERT_ACCOUNTS -> batchInsertAccountsSpec(dbClient);
        case BATCH_INSERT_PRODUCTS -> batchInsertProductsSpec(dbClient);
        case UPDATE_ACCOUNTS -> updateAccountsSpec(dbClient);
        case UPDATE_PRODUCTS -> updateProductsSpec(dbClient);
        case FIND_ACCOUNTS_BY_ID -> findAccountsByIdSpec(dbClient);
        case FIND_PRODUCTS_BY_ID -> findProductsByIdSpec(dbClient);
        case FIND_SORTED_BY_CREATED_AT_ACCOUNTS_PAGES -> findSortedByCreatedAtAccountsPagesSpec(dbClient);
        case FIND_ACCOUNTS_BY_OWNERS -> findAccountsByOwnersSpec(dbClient);
        case FIND_PRODUCTS_BY_TAGS -> findProductsByTagsSpec(dbClient);
        case FIND_ACCOUNTS_STATS_BY_IDS -> findAccountsStatsByIdsSpec(dbClient);
        case FIND_PRODUCTS_STATS_BY_IDS -> findProductsStatsByIdsSpec(dbClient);
        case INSERT_UPDATE_DELETE_FIND_ACCOUNTS -> insertUpdateDeleteFindByIdAccountsSpec(dbClient);
        case DELETE_ACCOUNTS -> deleteAccountsSpec(dbClient);
        case DELETE_PRODUCTS -> deleteProductsSpec(dbClient);
        case BATCH_DELETE_ACCOUNTS -> batchDeleteAccountsSpec(dbClient);
    };
}

static TestCaseSpec<Account> insertAccountsSpec(DbClient dbClient) {
    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(200_000),
            envQueriesRateOrDefault(20_000),
            new Query<>("insert-account", () -> randomAccount(), dbClient::insertAccount),
            ACCOUNTS_COLLECTION);
}

static TestCaseSpec<Product> insertProductsSpec(DbClient dbClient) {
    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(25_000),
            envQueriesRateOrDefault(2500),
            new Query<>("insert-product", () -> randomProduct(), dbClient::insertProduct),
            PRODUCTS_COLLECTION);
}

static TestCaseSpec<List<Account>> batchInsertAccountsSpec(DbClient dbClient) {
    var batch = 1000;
    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(1500),
            envQueriesRateOrDefault(150),
            new Query<>("batch-insert-accounts-%d".formatted(batch),
                    () -> Stream.generate(() -> randomAccount()).limit(batch).toList(),
                    dbClient::insertAccounts),
            ACCOUNTS_COLLECTION);
}

static TestCaseSpec<List<Product>> batchInsertProductsSpec(DbClient dbClient) {
    var batch = 100;
    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(1000),
            envQueriesRateOrDefault(100),
            new Query<>("batch-insert-products-%d".formatted(batch),
                    () -> Stream.generate(() -> randomProduct()).limit(batch).toList(),
                    dbClient::insertProducts),
            PRODUCTS_COLLECTION);
}

static TestCaseSpec<Account> updateAccountsSpec(DbClient dbClient) {
    var start = System.currentTimeMillis();
    var accountIds = requireAccountIds(dbClient);
    ADDITIONAL_QUERIES_DURATION_MILLIS = System.currentTimeMillis() - start;
    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(200_000),
            envQueriesRateOrDefault(20_000),
            new Query<>("update-account", () -> {
                var id = randomChoice(accountIds);
                return randomAccount(id);
            }, dbClient::updateAccount),
            ACCOUNTS_COLLECTION);
}

static TestCaseSpec<Product> updateProductsSpec(DbClient dbClient) {
    var start = System.currentTimeMillis();
    var productIds = requireProductIds(dbClient);
    ADDITIONAL_QUERIES_DURATION_MILLIS = System.currentTimeMillis() - start;
    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(25_000),
            envQueriesRateOrDefault(2500),
            new Query<>("update-product", () -> {
                var id = randomChoice(productIds);
                return randomProduct(id);
            }, dbClient::updateProduct),
            PRODUCTS_COLLECTION);
}

static List<UUID> requireAccountIds(DbClient dbClient) {
    var accountIds = dbClient.findAccountIds(500_000);
    if (accountIds.isEmpty()) {
        throw new IllegalStateException("No account ids to use for a test case!");
    }
    return accountIds;
}

static TestCaseSpec<UUID> findAccountsByIdSpec(DbClient dbClient) {
    var start = System.currentTimeMillis();
    var accountIds = requireAccountIds(dbClient);
    ADDITIONAL_QUERIES_DURATION_MILLIS = System.currentTimeMillis() - start;
    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(400_000),
            envQueriesRateOrDefault(40_000),
            new Query<>("find-account-by-id", () -> randomChoice(accountIds), dbClient::findAccountById),
            ACCOUNTS_COLLECTION);
}

static TestCaseSpec<UUID> findProductsByIdSpec(DbClient dbClient) {
    var start = System.currentTimeMillis();
    var productIds = requireProductIds(dbClient);
    ADDITIONAL_QUERIES_DURATION_MILLIS = System.currentTimeMillis() - start;
    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(400_000),
            envQueriesRateOrDefault(40_000),
            new Query<>("find-product-by-id", () -> randomChoice(productIds), dbClient::findProductById),
            PRODUCTS_COLLECTION);
}


static TestCaseSpec<DbClient.PageRequest> findSortedByCreatedAtAccountsPagesSpec(DbClient dbClient) {
    var maxLimit = 100;
    var start = System.currentTimeMillis();
    var minMaxCreatedAt = dbClient.findMinMaxAccountsCreatedAt();
    ADDITIONAL_QUERIES_DURATION_MILLIS = System.currentTimeMillis() - start;
    var minCreatedAtSeconds = minMaxCreatedAt.getFirst().getEpochSecond();
    var maxCreatedAtSeconds = minMaxCreatedAt.getLast().getEpochSecond();
    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(30_000),
            envQueriesRateOrDefault(3000),
            new Query<>("find-sorted-by-created-at-accounts-pages", () -> {
                var limit = randomNumber(10, maxLimit);
                var pageKey = randomNumber(minCreatedAtSeconds, maxCreatedAtSeconds);
                return new DbClient.PageRequest(Instant.ofEpochSecond(pageKey), limit, RANDOM.nextBoolean());
            }, dbClient::findAccountsPageSortedByCreatedAt),
            ACCOUNTS_COLLECTION);
}

static List<UUID> requireProductIds(DbClient dbClient) {
    var productIds = dbClient.findProductIds(500_000);
    if (productIds.isEmpty()) {
        throw new IllegalStateException("No product ids to use for a test case!");
    }
    return productIds;
}

static TestCaseSpec<List<String>> findAccountsByOwnersSpec(DbClient dbClient) {
    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(300_000),
            envQueriesRateOrDefault(30_000),
            new Query<>("find-accounts-by-owners",
                    () -> Stream.generate(() -> oneOfUserIds())
                            .limit(randomNumber(1, 5))
                            .distinct()
                            .toList(),
                    owners -> dbClient.findAccountsByOwners(owners, 25)),
            ACCOUNTS_COLLECTION);
}

static TestCaseSpec<List<String>> findProductsByTagsSpec(DbClient dbClient) {
    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(10_000),
            envQueriesRateOrDefault(1000),
            new Query<>("find-products-by-tags",
                    () -> Stream.generate(() -> randomChoice(TAGS))
                            .limit(randomNumber(1, 5))
                            .distinct()
                            .toList(), tags -> dbClient.findProductsByTags(tags, 50)),
            PRODUCTS_COLLECTION);
}

static TestCaseSpec<List<UUID>> findAccountsStatsByIdsSpec(DbClient dbClient) {
    var start = System.currentTimeMillis();
    var accountIds = requireAccountIds(dbClient);
    ADDITIONAL_QUERIES_DURATION_MILLIS = System.currentTimeMillis() - start;
    var idsBatch = 100;
    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(75_000),
            envQueriesRateOrDefault(7500),
            new Query<>("find-accounts-stats-by-ids-%d".formatted(idsBatch),
                    () -> Stream.generate(() -> randomChoice(accountIds))
                            .distinct()
                            .limit(idsBatch)
                            .toList(),
                    dbClient::findAccountsStats),
            ACCOUNTS_COLLECTION);
}

static TestCaseSpec<List<UUID>> findProductsStatsByIdsSpec(DbClient dbClient) {
    var start = System.currentTimeMillis();
    var productIds = requireProductIds(dbClient);
    ADDITIONAL_QUERIES_DURATION_MILLIS = System.currentTimeMillis() - start;
    var idsBatch = 100;
    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(10_000),
            envQueriesRateOrDefault(1_000),
            new Query<>("find-products-stats-by-ids-%d".formatted(idsBatch),
                    () -> Stream.generate(() -> randomChoice(productIds))
                            .distinct()
                            .limit(idsBatch)
                            .toList(),
                    dbClient::findProductsStats),
            PRODUCTS_COLLECTION);
}

static TestCaseSpec<?> insertUpdateDeleteFindByIdAccountsSpec(DbClient dbClient) {
    var start = System.currentTimeMillis();
    var accountIds = requireAccountIds(dbClient);
    ADDITIONAL_QUERIES_DURATION_MILLIS = System.currentTimeMillis() - start;
    var deletedAccountIds = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

    var findAccountByIdQuery = new Query<>("find-account-by-id",
            () -> randomChoiceExcluding(accountIds, deletedAccountIds), dbClient::findAccountById);

    return new TestCaseSpec(envQueriesToExecuteOrDefault(300_000),
            envQueriesRateOrDefault(30_000),
            List.of(
                    new Query<>("insert-account", () -> randomAccount(), dbClient::insertAccount),
                    new Query<>("update-account", () -> {
                        var id = randomChoiceExcluding(accountIds, deletedAccountIds);
                        return randomAccount(id);
                    }, dbClient::updateAccount),
                    new Query<>("delete-account", () -> randomChoice(accountIds), id -> {
                        dbClient.deleteAccounts(List.of(id));
                        deletedAccountIds.add(id);
                    }),
                    findAccountByIdQuery,
                    findAccountByIdQuery,
                    findAccountByIdQuery
            ),
            ACCOUNTS_COLLECTION);
}

static TestCaseSpec<UUID> deleteAccountsSpec(DbClient dbClient) {
    var start = System.currentTimeMillis();
    var accountIds = requireAccountIds(dbClient);
    ADDITIONAL_QUERIES_DURATION_MILLIS = System.currentTimeMillis() - start;
    var deletedIds = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(250_000),
            envQueriesRateOrDefault(25_000),
            new Query<>("delete-account", () -> randomChoiceExcluding(accountIds, deletedIds),
                    id -> {
                        dbClient.deleteAccounts(List.of(id));
                        deletedIds.add(id);
                    }),
            ACCOUNTS_COLLECTION);
}

static TestCaseSpec<UUID> deleteProductsSpec(DbClient dbClient) {
    var start = System.currentTimeMillis();
    var productIds = requireProductIds(dbClient);
    ADDITIONAL_QUERIES_DURATION_MILLIS = System.currentTimeMillis() - start;
    var deletedIds = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(150_000),
            envQueriesRateOrDefault(15_000),
            new Query<>("delete-product", () -> randomChoiceExcluding(productIds, deletedIds),
                    id -> {
                        dbClient.deleteProducts(List.of(id));
                        deletedIds.add(id);
                    }),
            PRODUCTS_COLLECTION);
}

static TestCaseSpec<List<UUID>> batchDeleteAccountsSpec(DbClient dbClient) {
    var batchSize = 1000;
    var start = System.currentTimeMillis();
    var accountIds = new AtomicReference<>(requireAccountIds(dbClient));
    ADDITIONAL_QUERIES_DURATION_MILLIS = System.currentTimeMillis() - start;

    var deletedIds = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

    var mutex = new Semaphore(1);

    return new TestCaseSpec<>(envQueriesToExecuteOrDefault(3000),
            envQueriesRateOrDefault(300),
            new Query<>("batch-delete-accounts-%d".formatted(batchSize),
                    () -> {
                        try {
                            mutex.acquire();
                            var refetchAccountIds = deletedIds.size() >= (accountIds.get().size() / 2);
                            if (refetchAccountIds) {
                                var s = System.currentTimeMillis();
                                accountIds.set(requireAccountIds(dbClient));
                                ADDITIONAL_QUERIES_DURATION_MILLIS += (System.currentTimeMillis() - s);
                                deletedIds.clear();
                            }
                            return Stream.generate(() -> randomChoiceExcluding(accountIds.get(), deletedIds))
                                    .distinct()
                                    .limit(batchSize)
                                    .toList();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            mutex.release();
                        }
                    },
                    ids -> {
                        dbClient.deleteAccounts(ids);
                        deletedIds.addAll(ids);
                    }),
            ACCOUNTS_COLLECTION);
}

static <T> QueryTestResult queryTest(List<Query<T>> queries) {
    // make queries more uniform
    randomDelay();

    var query = randomChoice(queries);
    long testDuration;

    try {
        var queryData = query.prepare();
        var start = System.nanoTime();

        query.execute(queryData);

        testDuration = System.nanoTime() - start;
    } catch (Exception e) {
        e.printStackTrace();
        testDuration = 0;
    }

    return new QueryTestResult(query.id, testDuration);
}

static void randomDelay() {
    try {
        var delay = RANDOM.nextInt(1000);
        Thread.sleep(delay);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

static Account randomAccount() {
    return randomAccount(UUID.randomUUID());
}

static Account randomAccount(UUID id) {
    var name = randomName();
    var type = randomChoice(ACCOUNT_TYPES);
    var owners = Stream.generate(() -> oneOfUserIds())
            .distinct()
            .limit(randomNumber(1, 5))
            .toList();
    var createdAt = randomTimestamp();
    var updatedAt = randomTimestampAfter(createdAt);
    var version = randomVersion();

    return new Account(id, name, type, owners, createdAt, updatedAt, version);
}

static String oneOfUserIds() {
    return "user-" + randomNumber(0, MAX_USER_ID);
}

static Product randomProduct() {
    return randomProduct(UUID.randomUUID());
}

static Product randomProduct(UUID id) {
    var name = randomName();
    var description = PRODUCTS_WITHOUT_DESCRIPTION ? null : randomString(2000, 3000);

    var categories = Stream.generate(() -> randomChoice(CATEGORIES))
            .limit(randomNumber(1, 5))
            .distinct()
            .toList();

    var tags = Stream.generate(() -> randomChoice(TAGS))
            .limit(randomNumber(1, 10))
            .distinct()
            .toList();

    var variations = Stream.generate(() -> {
                var type = randomChoice(VARIATIONS);
                var value = type.equals(COLOR_VARIATION) ? randomChoice(COLOR_VARIATIONS) : randomChoice(SIZE_VARIATIONS);
                return new Product.Variation(type, value);
            })
            .distinct()
            .limit(randomNumber(1, 10))
            .toList();

    var relatedProducts = RANDOM.nextBoolean() ? List.<UUID>of() : Stream.generate(UUID::randomUUID)
            .limit(randomNumber(0, 10))
            .toList();

    var createdAt = randomTimestamp();
    var updatedAt = randomTimestampAfter(createdAt);
    var version = randomVersion();

    return new Product(id, name, description, categories, tags, variations, relatedProducts,
            createdAt, updatedAt, version);
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
    return Instant.now().minusSeconds(RANDOM.nextLong(Duration.ofDays(1).getSeconds())).truncatedTo(ChronoUnit.MILLIS);
}

static Instant randomTimestampAfter(Instant timestamp) {
    return timestamp.plusSeconds(1 + RANDOM.nextLong(Duration.ofDays(1).getSeconds()));
}

static long randomVersion() {
    return randomNumber(1, 1000);
}

static int randomNumber(int min, int max) {
    return min + RANDOM.nextInt(max - min);
}

static long randomNumber(long min, long max) {
    return min + RANDOM.nextLong(max - min);
}

static <T> T randomChoice(List<T> elements) {
    var idx = RANDOM.nextInt(elements.size());
    return elements.get(idx);
}

static <T> T randomChoiceExcluding(List<T> elements, Collection<T> toExclude) {
    var maxAttempts = 100;
    for (int i = 0; i < maxAttempts; i++) {
        var choice = randomChoice(elements);
        if (!toExclude.contains(choice)) {
            return choice;
        }
    }
    throw new IllegalStateException("Cannot find a random choice in %d elements excluding %d after %d attempts"
            .formatted(elements.size(), toExclude.size(), maxAttempts));
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

    var queriesDuration = duration.minusMillis(ADDITIONAL_QUERIES_DURATION_MILLIS)
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
    MONGODB, POSTGRESQL
}

enum TestCase {
    INSERT_ACCOUNTS,
    INSERT_PRODUCTS,
    BATCH_INSERT_ACCOUNTS,
    BATCH_INSERT_PRODUCTS,
    UPDATE_ACCOUNTS,
    UPDATE_PRODUCTS,
    FIND_ACCOUNTS_BY_ID,
    FIND_PRODUCTS_BY_ID,
    FIND_SORTED_BY_CREATED_AT_ACCOUNTS_PAGES,
    FIND_ACCOUNTS_BY_OWNERS,
    FIND_PRODUCTS_BY_TAGS,
    FIND_ACCOUNTS_STATS_BY_IDS,
    FIND_PRODUCTS_STATS_BY_IDS,
    INSERT_UPDATE_DELETE_FIND_ACCOUNTS,
    DELETE_ACCOUNTS,
    DELETE_PRODUCTS,
    BATCH_DELETE_ACCOUNTS
}

record TestCaseSpec<T>(int queriesToExecute, int queriesRate, List<Query<T>> queries, String collection) {

    TestCaseSpec(int queriesToExecute, int queriesRate, Query<T> query, String collection) {
        this(queriesToExecute, queriesRate, List.of(query), collection);
    }
}

// Both are public for MongoDB POJO codecs
public record Account(@BsonId UUID id,
                      String name,
                      String type,
                      List<String> owners,
                      Instant createdAt,
                      Instant updatedAt,
                      long version) {
}

public record Product(@BsonId UUID id,
                      String name,
                      String description,
                      List<String> categories,
                      List<String> tags,
                      List<Variation> variations,
                      List<UUID> relatedProducts,
                      Instant createdAt,
                      Instant updatedAt,
                      long version) {

    public record Variation(String type, String value) {

    }
}

public record AccountsStats(String type,
                            int accounts,
                            Instant oldestAccountCreatedAt,
                            Instant newestAccountCreatedAt,
                            int minOwners,
                            int maxOwners) {
}

public record ProductsStats(String tag,
                            int products,
                            Instant oldestProductCreatedAt,
                            Instant newestProductCreatedAt,
                            int minVariations,
                            int maxVariations) {
}

sealed interface DbClient {

    void insertAccount(Account account);

    void insertAccounts(List<Account> accounts);

    void updateAccount(Account account);

    void deleteAccounts(List<UUID> ids);

    Optional<Account> findAccountById(UUID id);

    List<UUID> findAccountIds(int limit);

    List<Account> findAccountsPageSortedByCreatedAt(PageRequest request);

    List<Account> findAccountsByOwners(List<String> owners, int limit);

    List<AccountsStats> findAccountsStats(List<UUID> ids);

    List<Instant> findMinMaxAccountsCreatedAt();

    int count(String collection);

    void insertProduct(Product product);

    void insertProducts(List<Product> products);

    void updateProduct(Product product);

    void deleteProducts(List<UUID> ids);

    Optional<Product> findProductById(UUID id);

    List<Product> findProductsByTags(List<String> tags, int limit);

    List<ProductsStats> findProductsStats(List<UUID> ids);

    List<UUID> findProductIds(int limit);

    record PageRequest(Instant createdAtKey, int limit, boolean desc) {
    }
}

record PostgresClient(DataSource dataSource) implements DbClient {

    @Override
    public void insertAccount(Account account) {
        insertAccounts(List.of(account));
    }

    @Override
    public void insertAccounts(List<Account> accounts) {
        insertDocuments(ACCOUNTS_COLLECTION, accounts);
    }

    private <T> void insertDocuments(String collection, List<T> documents) {
        if (documents.isEmpty()) {
            return;
        }
        var query = "INSERT INTO %s VALUES ".formatted(collection)
                + documents.stream().map(a -> "(?::JSONB)")
                .collect(Collectors.joining(",\n"));

        executeQuery(conn -> {
            var statement = conn.prepareStatement(query);
            for (int i = 0; i < documents.size(); i++) {
                var a = documents.get(i);
                var json = OBJECT_MAPPER.writeValueAsString(a);
                statement.setString(i + 1, json);
            }
            statement.execute();
            return null;
        });
    }

    @Override
    public void updateAccount(Account account) {
        updateDocument(ACCOUNTS_COLLECTION, account, account.id);
    }

    private <T> void updateDocument(String collection, T document, UUID id) {
        executeQuery(conn -> {
            var query = "UPDATE %s SET data = ?::JSONB WHERE data->>'id' = ?".formatted(collection);
            var stmt = conn.prepareStatement(query);

            stmt.setString(1, OBJECT_MAPPER.writeValueAsString(document));
            stmt.setString(2, id.toString());

            stmt.execute();
            return null;
        });
    }

    @Override
    public void deleteAccounts(List<UUID> ids) {
        deleteDocuments(ACCOUNTS_COLLECTION, ids);
    }

    private void deleteDocuments(String collection, List<UUID> ids) {
        if (ids.isEmpty()) {
            return;
        }
        executeQuery(conn -> {
            var query = "DELETE FROM %s WHERE data->>'id' IN ".formatted(collection) +
                    ids.stream().map(i -> "?").collect(Collectors.joining(", ", "(", ")"));

            var stmt = conn.prepareStatement(query);

            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 1, ids.get(i).toString());
            }

            stmt.execute();

            return null;
        });
    }

    @Override
    public int count(String collection) {
        return executeQuery("SELECT COUNT(*) FROM " + collection, r -> {
            r.next();
            return r.getInt(1);
        });
    }

    @Override
    public Optional<Account> findAccountById(UUID id) {
        return findDocumentById(ACCOUNTS_COLLECTION, id, Account.class);
    }

    private <T> Optional<T> findDocumentById(String collection, UUID id, Class<T> type) {
        return executeQuery("SELECT * FROM %s WHERE data->>'id' = '%s'"
                        .formatted(collection, id.toString()),
                r -> {
                    if (r.next()) {
                        var json = r.getString(1);
                        return Optional.of(OBJECT_MAPPER.readValue(json, type));
                    }
                    return Optional.empty();
                });
    }

    @Override
    public List<Instant> findMinMaxAccountsCreatedAt() {
        return executeQuery("SELECT MIN(data->>'createdAt'), MAX(data->>'createdAt') FROM %s".formatted(ACCOUNTS_COLLECTION),
                r -> {
                    r.next();
                    var min = Instant.parse(r.getString(1));
                    var max = Instant.parse(r.getString(2));
                    return List.of(min, max);
                });
    }

    @Override
    public List<Account> findAccountsPageSortedByCreatedAt(PageRequest request) {
        var whereClause = "WHERE data ->> 'createdAt' %s '%s'".formatted(request.desc ? "<" : ">", request.createdAtKey.toString());
        return executeQueryMappingEachRow("SELECT * FROM %s %s ORDER BY data->>'createdAt' %s LIMIT %d"
                        .formatted(PRODUCTS_COLLECTION, whereClause, request.desc ? "DESC" : "ASC", request.limit),
                r -> {
                    var json = r.getString(1);
                    return OBJECT_MAPPER.readValue(json, Account.class);
                });
    }

    @Override
    public List<Account> findAccountsByOwners(List<String> owners, int limit) {
        return executeQueryMappingEachRow("SELECT * FROM %s WHERE data -> 'owners' ?| %s LIMIT %d"
                        .formatted(ACCOUNTS_COLLECTION, toPqArray(owners), limit),
                r -> {
                    var json = r.getString(1);
                    return OBJECT_MAPPER.readValue(json, Account.class);
                });
    }

    private String toPqArray(List<String> items) {
        return "array[%s]".formatted(items.stream().map("'%s'"::formatted).collect(Collectors.joining(",")));
    }

    @Override
    public List<AccountsStats> findAccountsStats(List<UUID> ids) {
        return executeQuery(conn -> {
            var query = """
                    SELECT
                        data->>'type' AS type,
                        COUNT(*) AS accounts,
                        MIN(data->>'createdAt') AS oldestAccountCreatedAt,
                        MAX(data->>'createdAt') AS newestAccountCreatedAt,
                        MIN(JSONB_ARRAY_LENGTH(data->'owners')) AS minOwners,
                        MAX(JSONB_ARRAY_LENGTH(data->'owners')) AS maxOwners
                    FROM %s
                    WHERE data->>'id' IN""".formatted(ACCOUNTS_COLLECTION)
                    + ids.stream().map(i -> "?").collect(Collectors.joining(", ", "(", ")"))
                    + "\nGROUP BY type";

            var stmt = conn.prepareStatement(query);

            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 1, ids.get(i).toString());
            }

            var result = stmt.executeQuery();

            var results = new ArrayList<AccountsStats>();
            while (result.next()) {
                results.add(mapAccountsStats(result));
            }
            return results;
        });
    }

    private AccountsStats mapAccountsStats(ResultSet result) throws SQLException {
        var type = result.getString("type");
        var accounts = result.getInt("accounts");
        var oldestAccountCreatedAt = Instant.parse(result.getString("oldestAccountCreatedAt"));
        var newestAccountCreatedAt = Instant.parse(result.getString("newestAccountCreatedAt"));
        var minOwners = result.getInt("minOwners");
        var maxOwners = result.getInt("maxOwners");
        return new AccountsStats(type, accounts,
                oldestAccountCreatedAt, newestAccountCreatedAt,
                minOwners, maxOwners);
    }

    @Override
    public List<UUID> findAccountIds(int limit) {
        return findDocumentIds(ACCOUNTS_COLLECTION, limit);
    }

    private List<UUID> findDocumentIds(String collection, int limit) {
        return executeQueryMappingEachRow("SELECT (data->>'id')::UUID FROM %s LIMIT %d"
                        .formatted(collection, limit),
                r -> r.getObject(1, UUID.class));
    }

    @Override
    public void insertProduct(Product product) {
        insertProducts(List.of(product));
    }

    @Override
    public void insertProducts(List<Product> products) {
        insertDocuments(PRODUCTS_COLLECTION, products);
    }

    @Override
    public void updateProduct(Product product) {
        updateDocument(PRODUCTS_COLLECTION, product, product.id);
    }

    @Override
    public void deleteProducts(List<UUID> ids) {
        deleteDocuments(PRODUCTS_COLLECTION, ids);
    }

    @Override
    public Optional<Product> findProductById(UUID id) {
        return findDocumentById(PRODUCTS_COLLECTION, id, Product.class);
    }

    @Override
    public List<Product> findProductsByTags(List<String> tags, int limit) {
        return executeQueryMappingEachRow("SELECT * FROM %s WHERE data -> 'tags' ?| %s LIMIT %d"
                        .formatted(PRODUCTS_COLLECTION, toPqArray(tags), limit),
                r -> {
                    var json = r.getString(1);
                    return OBJECT_MAPPER.readValue(json, Product.class);
                });
    }

    @Override
    public List<ProductsStats> findProductsStats(List<UUID> ids) {
        return executeQuery(conn -> {
            var query = """
                    SELECT
                        JSONB_ARRAY_ELEMENTS(data->'tags') AS tag,
                        COUNT(*) AS products,
                        MIN(data->>'createdAt') AS oldestProductCreatedAt,
                        MAX(data->>'createdAt') AS newestProductCreatedAt,
                        MIN(JSONB_ARRAY_LENGTH(data->'variations')) AS minVariations,
                        MAX(JSONB_ARRAY_LENGTH(data->'variations')) AS maxVariations
                    FROM %s
                    WHERE data->>'id' IN""".formatted(PRODUCTS_COLLECTION)
                    + ids.stream().map(i -> "?").collect(Collectors.joining(", ", "(", ")"))
                    + "\nGROUP BY tag";

            var stmt = conn.prepareStatement(query);

            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 1, ids.get(i).toString());
            }

            var result = stmt.executeQuery();

            var results = new ArrayList<ProductsStats>();
            while (result.next()) {
                results.add(mapProductsStats(result));
            }
            return results;
        });
    }

    private ProductsStats mapProductsStats(ResultSet result) throws SQLException {
        var tag = result.getString("tag");
        var products = result.getInt("products");
        var oldestProductCreatedAt = Instant.parse(result.getString("oldestProductCreatedAt"));
        var newestProductCreatedAt = Instant.parse(result.getString("newestProductCreatedAt"));
        var minVariations = result.getInt("minVariations");
        var maxVariations = result.getInt("maxVariations");
        return new ProductsStats(tag, products,
                oldestProductCreatedAt, newestProductCreatedAt,
                minVariations, maxVariations);
    }

    @Override
    public List<UUID> findProductIds(int limit) {
        return findDocumentIds(PRODUCTS_COLLECTION, limit);
    }

    private <T> T executeQuery(QueryExecutor<T> executor) {
        try (var conn = dataSource.getConnection()) {
            return executor.execute(conn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T executeQuery(String query, QueryResultMapper<T> mapper) {
        return executeQuery(conn -> {
            var statement = conn.createStatement();
            var result = statement.execute(query);
            if (result && mapper != null) {
                return mapper.map(statement.getResultSet());
            }
            return null;
        });
    }

    private <T> List<T> executeQueryMappingEachRow(String query, QueryResultMapper<T> mapper) {
        return executeQuery(query, r -> {
            var results = new ArrayList<T>();
            while (r.next()) {
                results.add(mapper.map(r));
            }
            return results;
        });
    }
}

static final class MongoDbClient implements DbClient {

    private final MongoCollection<Account> accountsTyped;
    private final MongoCollection<Document> accounts;
    private final MongoCollection<Product> productsTyped;
    private final MongoCollection<Document> products;

    MongoDbClient(MongoDatabase database) {
        this.accountsTyped = database.getCollection(ACCOUNTS_COLLECTION, Account.class);
        this.accounts = database.getCollection(ACCOUNTS_COLLECTION);
        this.productsTyped = database.getCollection(PRODUCTS_COLLECTION, Product.class);
        this.products = database.getCollection(PRODUCTS_COLLECTION);
    }

    @Override
    public void insertAccount(Account account) {
        accountsTyped.insertOne(account);
    }

    @Override
    public void insertAccounts(List<Account> accounts) {
        accountsTyped.insertMany(accounts);
    }

    @Override
    public void updateAccount(Account account) {
        accountsTyped.updateOne(Filters.eq("_id", account.id), new Document("$set", account));
    }

    @Override
    public void deleteAccounts(List<UUID> ids) {
        accountsTyped.deleteMany(Filters.in("_id", ids));
    }

    @Override
    public int count(String collection) {
        if (collection.equals(ACCOUNTS_COLLECTION)) {
            return (int) accountsTyped.countDocuments();
        }
        if (collection.equals(PRODUCTS_COLLECTION)) {
            return (int) productsTyped.countDocuments();
        }
        throw new IllegalArgumentException("Unknown collection: " + collection);
    }

    @Override
    public Optional<Account> findAccountById(UUID id) {
        return Optional.ofNullable(accountsTyped.find(Filters.eq("_id", id), Account.class).first());
    }

    @Override
    public List<Instant> findMinMaxAccountsCreatedAt() {
        return accounts.aggregate(List.of(Aggregates.group(null,
                        Accumulators.min("minCreatedAt", "$createdAt"),
                        Accumulators.max("maxCreatedAt", "$createdAt")
                ))).map(d -> List.of(d.get("minCreatedAt", Date.class).toInstant(), d.get("maxCreatedAt", Date.class).toInstant()))
                .first();
    }

    @Override
    public List<Account> findAccountsPageSortedByCreatedAt(PageRequest request) {
        var filter = request.desc
                ? Filters.lt("createdAt", request.createdAtKey)
                : Filters.gt("createdAt", request.createdAtKey);
        return accountsTyped.find(filter)
                .limit(request.limit)
                .sort(request.desc ? Sorts.descending("createdAt") : Sorts.ascending("createdAt"))
                .into(new ArrayList<>());
    }

    @Override
    public List<Account> findAccountsByOwners(List<String> owners, int limit) {
        return accountsTyped.find(Filters.in("owners", owners)).limit(limit).into(new ArrayList<>());
    }

    @Override
    public List<AccountsStats> findAccountsStats(List<UUID> ids) {
        var pipeline = List.of(
                Aggregates.match(Filters.in("_id", ids)),
                Aggregates.group("$type",
                        Accumulators.sum("accounts", 1),
                        Accumulators.min("oldestAccountCreatedAt", "$createdAt"),
                        Accumulators.max("newestAccountCreatedAt", "$createdAt"),
                        Accumulators.min("minOwners", new Document("$size", "$owners")),
                        Accumulators.max("maxOwners", new Document("$size", "$owners"))
                ),
                Aggregates.project(Projections.fields(
                        Projections.computed("type", "$_id"),
                        Projections.include("accounts", "oldestAccountCreatedAt", "newestAccountCreatedAt", "minOwners", "maxOwners"),
                        Projections.excludeId()
                ))
        );
        return accounts.aggregate(pipeline)
                .map(d -> new AccountsStats(
                        d.getString("type"),
                        d.getInteger("accounts"),
                        d.get("oldestAccountCreatedAt", Date.class).toInstant(),
                        d.get("newestAccountCreatedAt", Date.class).toInstant(),
                        d.getInteger("minOwners"),
                        d.getInteger("maxOwners")
                ))
                .into(new ArrayList<>());
    }

    @Override
    public List<UUID> findAccountIds(int limit) {
        return findDocumentIds(accounts, limit);
    }

    private List<UUID> findDocumentIds(MongoCollection<Document> collection, int limit) {
        return collection.find()
                .projection(Projections.fields(Projections.include("_id")))
                .limit(limit)
                .map(r -> r.get("_id", UUID.class))
                .into(new ArrayList<>());
    }

    @Override
    public void insertProduct(Product product) {
        productsTyped.insertOne(product);
    }

    @Override
    public void insertProducts(List<Product> products) {
        productsTyped.insertMany(products);
    }

    @Override
    public void updateProduct(Product product) {
        productsTyped.updateOne(Filters.eq("_id", product.id), new Document("$set", product));
    }

    @Override
    public void deleteProducts(List<UUID> ids) {
        productsTyped.deleteMany(Filters.in("_id", ids));
    }

    @Override
    public Optional<Product> findProductById(UUID id) {
        return Optional.ofNullable(productsTyped.find(Filters.eq("_id", id), Product.class).first());
    }

    @Override
    public List<Product> findProductsByTags(List<String> tags, int limit) {
        return productsTyped.find(Filters.in("tags", tags)).limit(limit).into(new ArrayList<>());
    }

    @Override
    public List<ProductsStats> findProductsStats(List<UUID> ids) {
        var pipeline = List.of(
                Aggregates.match(Filters.in("_id", ids)),
                Aggregates.unwind("$tags"),
                Aggregates.group("$tags",
                        Accumulators.sum("products", 1),
                        Accumulators.min("oldestProductCreatedAt", "$createdAt"),
                        Accumulators.max("newestProductCreatedAt", "$createdAt"),
                        Accumulators.min("minVariations", new Document("$size", "$variations")),
                        Accumulators.max("maxVariations", new Document("$size", "$variations"))
                ),
                Aggregates.project(Projections.fields(
                        Projections.computed("tag", "$_id"),
                        Projections.include("products", "oldestProductCreatedAt", "newestProductCreatedAt", "minVariations", "maxVariations"),
                        Projections.excludeId()
                ))
        );
        return products.aggregate(pipeline)
                .map(d -> new ProductsStats(
                        d.getString("tag"),
                        d.getInteger("products"),
                        d.get("oldestProductCreatedAt", Date.class).toInstant(),
                        d.get("newestProductCreatedAt", Date.class).toInstant(),
                        d.getInteger("minVariations"),
                        d.getInteger("maxVariations")
                ))
                .into(new ArrayList<>());
    }

    @Override
    public List<UUID> findProductIds(int limit) {
        return findDocumentIds(products, limit);
    }
}

static class Query<T> {

    final String id;
    private final Supplier<T> preparator;
    private final Consumer<T> executor;

    Query(String id, Supplier<T> preparator, Consumer<T> executor) {
        this.id = id;
        this.preparator = preparator;
        this.executor = executor;
    }

    T prepare() {
        return preparator.get();
    }

    void execute(T input) {
        executor.accept(input);
    }

    @Override
    public String toString() {
        return "Query[id=%s]".formatted(id);
    }
}

interface QueryResultMapper<T> {
    T map(ResultSet resultSet) throws Exception;
}

interface QueryExecutor<T> {
    T execute(Connection connection) throws Exception;
}

record QueryTestResult(String queryId, long testDuration) {
}