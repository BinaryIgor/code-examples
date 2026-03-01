# MongoDB vs PostgreSQL Performance Tests

Set of scripts and configs to make a detailed Mongo vs Postgres performance tests and comparisons for JSON documents.

Requirements:
* Docker
* Bash & Python 3 for scripts

## DBs

To build and run dbs in Docker, execute:
```
./build_and_run_mongodb.bash
./build_and_run_postgresql.bash
```

Check out those scripts if you want to customize their runtime and various config options a bit; for DB versions and schemas inspect db folders respectively.

We have the following collections/tables.

`mongodb/00-init.js`:
```js
db.createCollection("accounts");
db.accounts.createIndex(
  { createdAt: 1 },
  { name: "accounts_created_at_idx"}
);
db.accounts.createIndex(
  { owners: 1 },
  { name: "accounts_owners_idx"}
);

db.createCollection("products");
db.products.createIndex(
  { name: 1 },
  {
    name: "products_name_unique_idx",
    unique: true
  }
);
db.products.createIndex(
  { categories: 1 },
  { name: "products_categories_idx" }
);
db.products.createIndex(
  { tags: 1 },
  { name: "products_tags_idx" }
);
db.products.createIndex(
  { createdAt: 1 },
  { name: "products_created_at_idx" }
);
```

`postgresql/schema.sql`:
```sql
CREATE TABLE accounts (data JSONB NOT NULL);
CREATE UNIQUE INDEX accounts_id 
  ON accounts ((data->>'id'));
CREATE INDEX accounts_created_at_idx
  ON accounts ((data->>'createdAt'));
CREATE INDEX accounts_owners_idx
  ON accounts USING GIN ((data->'owners'));

CREATE TABLE products (data JSONB NOT NULL);
CREATE UNIQUE INDEX products_id
  ON products ((data->>'id'));
CREATE UNIQUE INDEX products_name_unique_idx
  ON products ((data->>'name'));
CREATE INDEX products_categories_idx
  ON products USING GIN ((data->'categories'));
CREATE INDEX products_tags_idx
  ON products USING GIN ((data->'tags'));
CREATE INDEX products_created_at_idx
  ON products ((data->>'createdAt'));
```

## Tests

All tests cases are defined in a single `tests/src/main/java/JsonDbsPerformanceTests.java` file. Java 25 & compatible Maven is required to build them, but it's all hidden in Docker:
```
./build_performance_tests.bash
```

Once this image is prepared, we can use another script, `run_test.py`, to choose a particular test case from the available options (Python 3 required).

## Execution

After going through the preparation, to run test cases locally against a particular database, just execute the `run_test.py` script:
```
./run_test.py                                                                                      

Choose test case. Available options:
1 - INSERT_ACCOUNTS
2 - INSERT_PRODUCTS
3 - BATCH_INSERT_ACCOUNTS
4 - BATCH_INSERT_PRODUCTS
5 - UPDATE_ACCOUNTS
6 - UPDATE_PRODUCTS
7 - FIND_ACCOUNTS_BY_ID
8 - FIND_PRODUCTS_BY_ID
9 - FIND_SORTED_BY_CREATED_AT_ACCOUNTS_PAGES
10 - FIND_ACCOUNTS_BY_OWNERS
11 - FIND_PRODUCTS_BY_TAGS
12 - FIND_ACCOUNTS_STATS_BY_IDS
13 - FIND_PRODUCTS_STATS_BY_IDS
14 - INSERT_UPDATE_DELETE_FIND_ACCOUNTS
15 - DELETE_ACCOUNTS
16 - DELETE_PRODUCTS
17 - BATCH_DELETE_ACCOUNTS
1
Chosen test case: INSERT_ACCOUNTS

Choose db. Available options:
1 - MONGODB
2 - POSTGRESQL
2

Running with PostgreSQL

json-dbs-performance-tests

Starting Json DBs Performance Tests, connecting to POSTGRESQL with a pool of 64 connections...

[main] INFO com.zaxxer.hikari.HikariDataSource - POSTGRESQL - Starting...
[main] INFO com.zaxxer.hikari.pool.HikariPool - POSTGRESQL - Added connection org.postgresql.jdbc.PgConnection@56620197
[main] INFO com.zaxxer.hikari.HikariDataSource - POSTGRESQL - Start completed.

Connected with POSTGRESQL, about to run INSERT_ACCOUNTS test case.
The following test case specification will be executed: TestCaseSpec[queriesToExecute=200000, queriesRate=20000, queries=[Query[id=insert-account]], collection=accounts]

Collection count before test: 0

2026-03-01T12:25:17.238, 20000/200000 queries were issued, waiting 1s before sending next query batch...
2026-03-01T12:25:19.273, 40000/200000 queries were issued, waiting 1s before sending next query batch...

...

2026-03-01T12:25:25.657, 160000/200000 queries were issued, waiting 1s before sending next query batch...
2026-03-01T12:25:26.759, 180000/200000 queries were issued, waiting 1s before sending next query batch...

...

Test case INSERT_ACCOUNTS with POSTGRESQL finished! It had queries: [Query[id=insert-account]]

Collection count after test: 200000

Some stats...

Total test duration: PT11.848S
Queries duration: PT11.848S

Executed queries: 200000

Wanted queries rate: 20000/s
Actual queries rate: 16880/s

Min: 1.078 ms
Max: 1956.482 ms
Mean: 85.261 ms

Percentile 50 (Median): 2.597 ms
Percentile 75: 3.818 ms
Percentile 90: 31.275 ms
Percentile 99: 995.659 ms
Percentile 99.9: 1552.152 ms

Tests have finished running, exporting results to a file...

Results exported to the insert_accounts_postgresql.txt file
```

After running this particular test case, we might examine our local Postgres instance:
```
docker exec -it json-dbs-postgresql psql -U json
psql (18.1 (Debian 18.1-1.pgdg13+2))
Type "help" for help.

json=# \dt
          List of tables
 Schema |   Name   | Type  | Owner 
--------+----------+-------+-------
 public | accounts | table | json
 public | products | table | json
(2 rows)

json=# select count(*) from accounts;
 count  
--------
 200000
(1 row)
```

The same could be done for MongoDB and other test cases:
```
docker exec -it json-dbs-mongodb mongosh "mongodb://json:json@localhost:27017/json?authSource=admin"
```

## Customization

You can customize each test case a bit without changing the code, using environment variables:
```
export QUERIES_TO_EXECUTE=<how much queries to execute in total>
export QUERIES_RATE=<queries per second rate>
```

## Results

If you care just about the numbers, in the `/results` there are results of my executions, together with `_env_.md` information in each case.
You can run them yourself and compare the results :)
