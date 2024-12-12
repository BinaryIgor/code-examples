# SQLite Performance Limits

Let's check what the simplest database can handle!

Requirements:

* Java 21 + compatible Maven version
* Docker
* Bash for scripts

All we have is a test tool written in Java + a few bash scripts to make the whole process easier.
SQLite is embedded, in-process, single-file (almost) database, so that is all we need.
We will run it in Docker, since with it, it is trivial to limit process resources and observe their usage.

## Preparation

First, let's build the test tool:

```
bash build_tests.bash
```

it builds java jar and prepares associated Docker image.

Then, we want to run tests against a dataset of respectful size. Let's insert 1 million records into our test table:

```
bash run_batch_inserts_test_case.bash
```

You should see something like this:

```
About to run SQLite Limits Tests!
Connecting to /db/limits_tests.db db with the journal_mode=WAL

[main] INFO com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Starting...
[main] INFO com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Added connection org.sqlite.jdbc4.JDBC4Connection@52af6cff
[main] INFO com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Start completed.

DB connection established, running init queries

Init queries executed, db schema:

CREATE TABLE IF NOT EXISTS account (
  id INTEGER PRIMARY KEY,
  email TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL,
  description TEXT,
  created_at INTEGER NOT NULL,
  version INTEGER
);
CREATE INDEX IF NOT EXISTS account_name ON account(name);

The following test case (BATCH_INSERTS) will be executed: TestCase[queriesToExecute=1000, queriesRate=20, queryGroups=[QueryGroup[id=batch-insert, tables=[account]]]]

Tables count before test...
account: 0

Running it...

2024-12-12T06:05:20.211497828, 20/1000 queries were issued, waiting 1s before sending next query batch...
2024-12-12T06:05:21.963230322, 40/1000 queries were issued, waiting 1s before sending next query batch...
...
2024-12-12T06:06:21.683973652, 960/1000 queries were issued, waiting 1s before sending next query batch...
2024-12-12T06:06:22.817088595, 980/1000 queries were issued, waiting 1s before sending next query batch...

...

Test case finished! It had queries:
QueryGroup[id=batch-insert, tables=[account]]

Tables count after test...
account: 1000000

Some stats...

Test duration: PT1M4.968S
Executed queries: 1000
Wanted queries rate: 20/s
Actual queries rate: 15/s

Min: 10.436013 ms
Max: 538.455679 ms
Mean: 62.633277 ms

Percentile 50 (Median): 52.633034 ms
Percentile 75: 64.021451 ms
Percentile 90: 88.319954 ms
Percentile 95: 121.268032 ms
Percentile 99: 256.904597 ms
Percentile 99.9: 538.455679 ms

...
```

It took just little over a minute to insert 1 million rows with batch inserts ~ *15 000 rows per second*. Nice!

What is notable here is that we run SQLite in the **WAL mode**. It is not a default mode, but it increases performance in most
cases: https://www.sqlite.org/wal.html.

**All tests run with 1GB of RAM and 2 CPUs**, as specified in the `run_test_case.bash` script.
For more details about the tests setup and implementation, check out `SQLiteLimitsTests.java` file.

## Tests

To run any of available test cases:

```
bash run_writes_100_test_case.bash
bash run_reads_100_test_case.bash
bash run_writes_50_reads_50_test_case.bash
bash run_writes_10_reads_90_test_case.bash
```

They all have predefined `QUERIES_TO_EXECUTE` and `QUERIES_RATE` env variables.
Feel free to change them and check out the results!

## Results

If you care just about the numbers, in the `/test-results` there is a summary and test results. 