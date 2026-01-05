# MySQL and PostgreSQL differences

Let's check how their performance characteristics compare across a few scenarios!

Requirements:
* Docker
* Java 21 + compatible Maven version

## Approach

We have two tables to compare how MySQL and Postgres handle tables with multiple (4) indexes versus just one index (Postgres version, check out schema files):
```
CREATE TABLE table_few_indexes (
  id BIGSERIAL PRIMARY KEY,
  name TEXT UNIQUE NOT NULL,
  status TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version BIGINT NOT NULL
);
CREATE INDEX table_few_indexes_created_at ON table_few_indexes(created_at);
CREATE INDEX table_few_indexes_updated_at ON table_few_indexes(updated_at);

CREATE TABLE table_single_index (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  status TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version BIGINT NOT NULL
);
```

Available test cases:
```
enum TestCases {
  BATCH_INSERT_TABLE_SINGLE_INDEX,
  BATCH_INSERT_TABLE_FEW_INDEXES,
  INSERT_TABLE_SINGLE_INDEX,
  INSERT_TABLE_FEW_INDEXES,
  UPDATE_TABLE_SINGLE_INDEX,
  UPDATE_TABLE_FEW_INDEXES,
  DELETE_TABLE_SINGLE_INDEX,
  DELETE_TABLE_FEW_INDEXES,
  SELECT_TABLE_SINGLE_INDEX_BY_PRIMARY_KEY,
  SELECT_TABLE_FEW_INDEXES_BY_PRIMARY_KEY
}
```

Both databases are dockerized and run with 4 GB of memory and 2 CPUs as defined in 
`build_and_run_mysql.bash` and `build_and_run_postgresql.bash` respectively.

## Running

Build tests tool first - it is a simple ~ 500 lines-long Java code. Go to the `/tests` dir and run:
```
mvn clean package
```

Then, build and start dbs (in Docker). From root dir run:
```
bash build_and_run_mysql.bash
bash build_and_run_postgresql.bash
```
Make sure that they are up in running:
```
docker ps

CONTAINER ID   IMAGE                       COMMAND                  CREATED          STATUS          PORTS                                                  NAMES
d2ccc6e4c4bd   postgresql-differences-db   "docker-entrypoint.s…"   26 seconds ago   Up 25 seconds   0.0.0.0:5432->5432/tcp, :::5432->5432/tcp              postgresql-differences-db
700e83ac2a93   mysql-differences-db        "docker-entrypoint.s…"   32 seconds ago   Up 32 seconds   0.0.0.0:3306->3306/tcp, :::3306->3306/tcp, 33060/tcp   mysql-differences-db
```

Now, we are ready to run test cases! First, I recommend to run batch insert cases to have at least 2 million rows in the tables.
To do that, run:
```
export TEST_CASE=BATCH_INSERT_TABLE_FEW_INDEXES
bash run_postgresql_test.bash
```

You should see logs of the following kind:
```
Starting db tests, connecting to Postgres data source

[main] INFO com.zaxxer.hikari.HikariDataSource - Postgres - Starting...
[main] INFO com.zaxxer.hikari.pool.HikariPool - Postgres - Added connection org.postgresql.jdbc.PgConnection@3b94d659
[main] INFO com.zaxxer.hikari.HikariDataSource - Postgres - Start completed.

Postgres data source connected, running BATCH_INSERT_TABLE_FEW_INDEXES test case with it!
The following test case will be executed: TestCase[queriesToExecute=2000, queriesMaxRate=10, queryGroups=[QueryGroup[id=batch-insert-table-few-indexes, tables=[table_few_indexes]]]]
Tables count before test...
table_few_indexes: 0

Running it...
```

To run the same test case for MySQL:
```
bash run_mysql_test.bash
```

To run other cases, just export appropriate env variable:
```
export TEST_CASE=<TEST_CASE>
```

To check what is in the databases, just do:
```
docker exec -it mysql-differences-db mysql -D test -p
Enter password: 
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 19
Server version: 9.1.0 MySQL Community Server - GPL

Copyright (c) 2000, 2024, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> SELECT COUNT(*) FROM table_few_indexes;
+----------+
| COUNT(*) |
+----------+
|  2000000 |
+----------+
1 row in set (0.41 sec)


docker exec -it postgresql-differences-db psql -U postgres -d test
psql (17.0 (Debian 17.0-1.pgdg120+1))
Type "help" for help.

test=# SELECT COUNT(*) FROM table_few_indexes;
  count  
---------
 2000000
(1 row)
```

## Results

If you are here only to see results of all test cases, you can find them in the results directory.
Tests were run with both dbs having 4 GB of memory and 2 CPUs (as limited in Docker scripts).
