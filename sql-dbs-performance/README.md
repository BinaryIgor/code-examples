# SQL DBs Performance Tests

Set of scripts and configs to make a detailed SQL DBs performance tests and comparisons - both locally and on remote machines!

Requirements:
* Docker
* Bash & Python 3 for scripts
* *To run remotely:* DigitalOcean account or similar remote machine access through SSH keys

Detailed instructions for preparation & runs on different environments:
* [Local Tests](#local-tests)
* [Remote Tests](#remote-tests)

## Local Tests

### Preparation

#### DBs

To run tests, DBs with expected schemas are required. Currently, supported DBs are:
* MySQL
* PostgreSQL
* MariaDB

To build and run them in Docker, execute:
```
./build_and_run_mysql.bash
./build_and_run_postgresql.bash
./build_and_run_mariadb.bash
```

Check out those scripts if you want to customize their runtime and various config options a bit; for DB versions and schemas inspect db folders respectively.

We have the following schema - `postgresql/schema.sql` example:
```sql
CREATE TABLE "user" (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE TABLE "order" (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);
CREATE INDEX order_user_id ON "order"(user_id);

CREATE TABLE "item" (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  description TEXT,
  price NUMERIC(10, 2) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE TABLE "order_item" (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES "order"(id) ON DELETE CASCADE,
  item_id BIGINT NOT NULL REFERENCES "item"(id) ON DELETE CASCADE
);
CREATE INDEX order_item_order_id ON "order_item"(order_id);
CREATE INDEX order_item_item_id ON "order_item"(item_id);
```

#### Tests

All tests cases are defined in a single `tests/src/main/java/SqlDbPerformanceTests.java` file. Java 25 & compatible Maven is required to build them, but it's all hidden in Docker:
```
./build_performance_tests.bash
```

Once this image is prepared, we can use another script, `run_test.py`, to choose a particular test case from the available options (Python 3 required).

### Execution

After going through the preparation, to run test cases locally against a particular database, just execute the `run_test.py` script:
```
./run_test.py                                                                                      

Choose test case. Available options:
1 - INSERT_USERS
2 - INSERT_ITEMS_IN_BATCHES
3 - INSERT_ORDERS_IN_BATCHES
4 - INSERT_ORDER_ITEMS_IN_BATCHES
5 - SELECT_USERS_BY_ID
6 - SELECT_USERS_BY_EMAIL
7 - SELECT_SORTED_BY_ID_USER_PAGES
8 - SELECT_ORDERS_JOINED_WITH_USERS
9 - SELECT_ORDERS_JOINED_WITH_ITEMS
10 - SELECT_USERS_WITH_ORDERS_STATS_BY_ID
11 - UPDATE_USER_EMAILS_BY_ID
12 - UPDATE_USER_UPDATED_ATS_BY_ID
13 - UPDATE_USER_MULTIPLE_COLUMNS_BY_ID
14 - DELETE_ORDERS_BY_ID
15 - DELETE_ORDERS_IN_BATCHES_BY_ID
16 - INSERT_USERS_AND_ORDERS_WITH_ITEMS_IN_TRANSACTIONS
17 - INSERT_UPDATE_DELETE_AND_SELECT_USERS_BY_ID
1
Chosen test case: INSERT_USERS

Choose db. Available options:
1 - MySQL
2 - PostgreSQL
3 - MariaDB
2

Running with PostgreSQL

sql-db-performance-tests

Starting DB Performance Tests, connecting to POSTGRESQL data source with a pool of 64 connections...

[main] INFO com.zaxxer.hikari.HikariDataSource - POSTGRESQL - Starting...
[main] INFO com.zaxxer.hikari.pool.HikariPool - POSTGRESQL - Added connection org.postgresql.jdbc.PgConnection@4bec1f0c

POSTGRESQL data source connected, about to run INSERT_USERS test case.
[main] INFO com.zaxxer.hikari.HikariDataSource - POSTGRESQL - Start completed.
The following test case specification will be executed: TestCaseSpec[queriesToExecute=500000, queriesRate=10000, queryGroups=[QueryGroup[id=insert-users, tables=["user"]]]]

Tables count before test:
"user": 0

2026-01-05T06:11:51.609, 10000/500000 queries were issued, waiting 1s before sending next query batch...
2026-01-05T06:11:53.065, 20000/500000 queries were issued, waiting 1s before sending next query batch...
2026-01-05T06:11:54.080, 30000/500000 queries were issued, waiting 1s before sending next query batch...

...

2026-01-05T06:12:40.324, 470000/500000 queries were issued, waiting 1s before sending next query batch...
2026-01-05T06:12:41.381, 480000/500000 queries were issued, waiting 1s before sending next query batch...
2026-01-05T06:12:42.444, 490000/500000 queries were issued, waiting 1s before sending next query batch...

...

Test case INSERT_USERS with POSTGRESQL data source finished! It had queries: [QueryGroup[id=insert-users, tables=["user"]]]

Tables count after test:
"user": 500000

Some stats...

Total test duration: PT53.043S
Queries duration: PT53.043S

Executed queries: 500000

Wanted queries rate: 10000/s
Actual queries rate: 9426/s

Min: 1.066 ms
Max: 69.944 ms
Mean: 2.383 ms

Percentile 50 (Median): 2.358 ms
Percentile 75: 2.668 ms
Percentile 90: 2.905 ms
Percentile 99: 3.795 ms
Percentile 99.9: 13.348 ms
```
After running this particular test case, we might examine our local Postgres instance:
```
docker exec -it postgresql-performance psql -U postgres -d performance
psql (18.1 (Debian 18.1-1.pgdg13+2))
Type "help" for help.

performance=# \dt
             List of tables
 Schema |    Name    | Type  |  Owner   
--------+------------+-------+----------
 public | item       | table | postgres
 public | order      | table | postgres
 public | order_item | table | postgres
 public | user       | table | postgres
(4 rows)

performance=# select count(*) from "user";
 count  
--------
 500000
(1 row)
```

The same could be done for other DBs and test cases:
```

docker exec -it mariadb-performance mariadb --database performance -p
docker exec -it mysql-performance mysql --database performance -p
```

## Remote Tests

### Preparation

#### Infra

First things first, we need to have a few virtual machines. As said, we're going to use DigitalOcean to set the infrastructure up.
You might also run this on any Linux-based machines that have network access to each other on db-related ports; but in order to do that, you would have to customize scripts a bit.

With that in mind, let's start; from the `remote-scripts` dir, set the Python env up with just the `requests` dependency:
```
./init_python_env.bash
source venv/bin/activate
```

Now, the following may take a while, since we are creating quite a few machines; three for running tests and three for DBs:
```
export DO_API_TOKEN="<your DigitalOcean API key>"
export SSH_KEY_FINGERPRINT="<fingerprint of your ssh key, uploaded to DigitalOcean, giving you ssh access to created machines>"

./prepare_infra.py
```

Again, if you don't want to run this on DigitalOcean, you must set these machines up on your own.

After the script succeeds, we have six ready-to-be-used machines for our performance tests.

#### DBs

Now we will build, package and deploy databases - all three of them.

First, let's build & package - all in Docker; it might take a while, especially when executed for the first time. From the `remote-scripts`, run:
```
./build_and_package_dbs.bash
```
It builds docker images of all dbs and packages them into tar archives, to-be-deployed to our remote machines.
If you are curious and/or want to customize db configs a bit, check out `build_and_package.bash` scripts, located in each db dir.

To deploy dbs, we must get public ip addresses of the target machines - each db having its own, dedicated one. Then, we simply run:
```
export MYSQL_HOST=<mysql machine public ip>
export POSTGRESQL_HOST=<postgresql machine public ip>
export MARIADB_HOST=<mariadb machine public ip>

./deploy_dbs.bash
```

This can also take a while, since we are sending full-blown docker images here - but fortunately, it must be done only once!
Once the scripts finishes, dbs are ready and running:
```
ssh ops@$MYSQL_HOST "docker ps"
CONTAINER ID   IMAGE                      COMMAND                  CREATED         STATUS         PORTS     NAMES
de987e27e98e   mysql-performance:latest   "docker-entrypoint.s…"   8 minutes ago   Up 8 minutes             mysql-performance

ssh ops@$POSTGRESQL_HOST "docker ps"
CONTAINER ID   IMAGE                           COMMAND                  CREATED         STATUS         PORTS     NAMES
baafa551cf83   postgresql-performance:latest   "docker-entrypoint.s…"   7 minutes ago   Up 7 minutes             postgresql-performance

ssh ops@$MARIADB_HOST "docker ps"
CONTAINER ID   IMAGE                        COMMAND                  CREATED         STATUS         PORTS     NAMES
3faba51eeba5   mariadb-performance:latest   "docker-entrypoint.s…"   7 minutes ago   Up 7 minutes             mariadb-performance
```

DBs are then ready and steady - time to prepare performance tests.

#### Tests

Similar to dbs, we build & package tests in Docker (Java 25 & Maven app):
```
./build_and_package_tests.bash
```

Then we similarly deploy it; with a caveat that the docker images are just sent and loaded on the target tests machines - they are not being run just yet:
```
export MYSQL_TESTS_HOST=<mysql tests machine public ip>
export POSTGRESQL_TESTS_HOST=<postgresql tests machine public ip>
export MARIADB_TESTS_HOST=<mariadb tests machine public ip>

./deploy_tests.bash
```

Together with tests, the `run_test.py` script is deployed to each tests machine - we will now use it to finally run the tests!

### Execution

After going through the preparation, to run test cases remotely against a particular database, we just execute the `run_test.py` script on each machine:
```
ssh ops@<tests machine public ip>

cd deploy/tests

export DB_HOST=<db machine PRIVATE ip>

./run_test.py

Choose test case. Available options:
1 - INSERT_USERS
2 - INSERT_ITEMS_IN_BATCHES
3 - INSERT_ORDERS_IN_BATCHES
4 - INSERT_ORDER_ITEMS_IN_BATCHES
5 - SELECT_USERS_BY_ID
6 - SELECT_USERS_BY_EMAIL
7 - SELECT_SORTED_BY_ID_USER_PAGES
8 - SELECT_ORDERS_JOINED_WITH_USERS
9 - SELECT_ORDERS_JOINED_WITH_ITEMS
10 - SELECT_USERS_WITH_ORDERS_STATS_BY_ID
11 - UPDATE_USER_EMAILS_BY_ID
12 - UPDATE_USER_UPDATED_ATS_BY_ID
13 - UPDATE_USER_MULTIPLE_COLUMNS_BY_ID
14 - DELETE_ORDERS_BY_ID
15 - DELETE_ORDERS_IN_BATCHES_BY_ID
16 - INSERT_USERS_AND_ORDERS_WITH_ITEMS_IN_TRANSACTIONS
17 - INSERT_UPDATE_DELETE_AND_SELECT_USERS_BY_ID
1
Chosen test case: INSERT_USERS

Choose db. Available options:
1 - MySQL
2 - PostgreSQL
3 - MariaDB
1

Running with MySQL

Starting DB Performance Tests, connecting to MYSQL data source with a pool of 128 connections...

[main] INFO com.zaxxer.hikari.HikariDataSource - MYSQL - Starting...
[main] INFO com.zaxxer.hikari.pool.HikariPool - MYSQL - Added connection com.mysql.cj.jdbc.ConnectionImpl@757277dc

[main] INFO com.zaxxer.hikari.HikariDataSource - MYSQL - Start completed.
MYSQL data source connected, about to run INSERT_USERS test case.
The following test case specification will be executed: TestCaseSpec[queriesToExecute=500000, queriesRate=10000, queryGroups=[QueryGroup[id=insert-users, tables=[`user`]]]]

Tables count before test:
`user`: 0

2026-01-12T09:39:05.477, 10000/500000 queries were issued, waiting 1s before sending next query batch...
2026-01-12T09:39:08.601, 20000/500000 queries were issued, waiting 1s before sending next query batch...
2026-01-12T09:39:10.087, 30000/500000 queries were issued, waiting 1s before sending next query batch...

...

2026-01-12T09:40:03.453, 470000/500000 queries were issued, waiting 1s before sending next query batch...
2026-01-12T09:40:04.664, 480000/500000 queries were issued, waiting 1s before sending next query batch...
2026-01-12T09:40:06.044, 490000/500000 queries were issued, waiting 1s before sending next query batch...

...

Test case INSERT_USERS with MYSQL data source finished! It had queries: [QueryGroup[id=insert-users, tables=[`user`]]]

Tables count after test:
`user`: 500000

Some stats...

Total test duration: PT1M3.299S
Queries duration: PT1M3.299S

Executed queries: 500000

Wanted queries rate: 10000/s
Actual queries rate: 7899/s

Min: 2.818 ms
Max: 213.591 ms
Mean: 12.469 ms

Percentile 50 (Median): 10.67 ms
Percentile 75: 12.905 ms
Percentile 90: 16.104 ms
Percentile 99: 79.42 ms
Percentile 99.9: 112.038 ms

Tests have finished running, exporting results to a file...

Results exported to to the insert_users_mysql.txt file
```

As the output says - there are lots of test cases and the output of a chosen one is saved in the file. 

## Customization

You can customize each test case a bit without changing the code, using environment variables:
```
export QUERIES_TO_EXECUTE=<how much queries to execute in total>
export QUERIES_RATE=<queries per second rate>
export DATA_SOURCE_CONNECTION_POOL_SIZE=<connection pool size - different default values for different DBs>
export DB_HOST=<if different than localhost - for remote tests mostly>
```

## Results

If you care just about the numbers, in the `/results` there are results of my executions, together with `_env_.md` information in each case.
You can run them yourself and compare with your results :)
