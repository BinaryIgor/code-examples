# What a Single Machine can handle?

I often talk about simple architectures and how monoliths should be preferred to distributed services.
In this context, let's see how much load, http requests/second to be exact, can a Single Machine handle.

## What are we going to do?

Execute LoadTest against single machine with basic Java 21 rest service and SQLite db.

We will test single machines with various resources (memory and cpu):
* 1 CPU, 1 GB of memory
* 2 CPUs, 2 GB of memory
* 4 CPUs, 8 GB of memory

Tests, http requests, will be executed from a few machines (4), in parallel.

In the database, we will have a little over 1 million random rows of the table (initialized by `single-app`:
```
CREATE TABLE account (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    created_at INTEGER NOT NULL,
    version INTEGER NOT NULL
);

CREATE INDEX account_name ON account(name);
```

What's more, we will have realistic endpoints:
```
GET: /accounts/{id}
GET: /accounts?name={name}
# randomly executes either insert or delete
POST: /accounts/execute-random-write
```

All of that, to answer the question:
> How many http requests per second can a Single Machine handle?

## What we have here?

* single-app: simple Java 21 + Virtual Threads + Spring Boot 3 application using SQLite
* load-test: script to execute various loads of http requests and output detailed results
* scripts to automate operations:
    * creating infrastructure on DigitalOcean from scratch: single-machine, db volume and 4 test machines, where we will
      execute `load-test`
    * building and deploying single-app and load-test
    * executing `load-test` on test machines + generating test reports

## Some requirements

* DigitalOcean account (or an API token of someone's else account) - we need to create machines there
* In theory, we just need to have virtual machine for `single-app` + a few virtual machines for `load-test`,
  so you do not need to use DigitalOcean. In that case, you will not be able to use `prepare_infra.py` script and need
  to prepare those machines accordingly, on your own
* Python 3.10+ to run `prepare_infra.py` script - it will create all machines needed for tests and set them up

That's pretty much it - we will run most things in Docker and on the prepared virtual machines, not our local machine :)
We will use bash and its basic utilities like `scp` and `ssh`, so you need to have Linux/Linux-like machine.

## Prepare infra

We need to have:
* `single-machine` to test
* a few (4) machines to execute `load-test` on `single-machine`
* on each machine we need to have Docker, on `single-machine` we also need to have external volume for the
  SQLite database

All of that is taken care of by `prepare_infra.py` script. It has some dependencies (`requirements.txt`), so we should run:
```
cd scripts
bash setup_python_env.bash
```

This will create virtual Python environment and install required dependencies (just requests for making http requests).
Then, we can actually create infra; we also need to have *DigitalOcean* token with write permissions.
Remember that we will create resources for which you will be charged real money!

Once we are ready run (also from scripts folder):
```
# activate venv environment
source venv/bin/activate
export DO_API_TOKEN=<your digital ocean API token>
export SSH_KEY_FINGERPRINT=<your ssh key fingerprint uploaded to DigitalOcean>
python3 prepare_infra.py large
```

`large` is a machine size argument; supported values:
* small - 1 CPU + 1 GB memory droplet
* medium - 2 CPUs + 2 GB memory droplet
* large - 4 CPUs + 8 GB memory droplet

This script can take a while to finish, because it also creates 4 test droplets - we will run load tests from them - and volume for the SQLite db.
Additionally, it is idempotent - we can run it multiple times, and it will figure out what needs to created and what
needs to skipped - it just doesn't do updates, only creates and deletes.

After completion, it can take up to 3 - 5 minutes for machines to be fully ready, so be patient ;)

## Build and deploy single app

### Build

Once the infra is ready, we can start experimenting. First, we need to build app package. Fortunately, it needs to be done only once.
From `scripts` dir, run:
```
bash build_and_package_app.bash
```
This will build and package our Spring Boot app. 
Running it for the first time can take a few minutes, since we need to download a few Docker images + Maven dependencies for the Java app.
Fortunately, it only needs to be done once and consequent builds will be significantly faster, in case of changes.

At this point, in `single-app/dist` we should have ready to be deployed packages like this:
```
ls -l

-rw-rw-r-- 1 igor igor       241 gru 21 11:00 load_and_run_app.bash
-rw-rw-r-- 1 igor igor       474 gru 21 11:00 run_app.bash
-rw-rw-r-- 1 igor igor 222339765 gru 21 11:00 single-app.tar.gz
```

This is just gzipped docker image that we will deploy to our `single-machine`.

### Deploy

We have our packages ready, let's then deploy them!

From the DigitalOcean UI (https://cloud.digitalocean.com/droplets) we can grab the IP address of our droplet.
Let's say that it is `142.93.173.0`. We can now run deploy scripts:
```
# remember to change deploy host to your ip address!
export DEPLOY_HOST=142.93.173.0
bash deploy_app.bash
```

This will use `ssh` and `scp` to copy previously built package and deploy it to our glorious Single Machine.
App image weigh a few hundreds MB, so it can take a while.

After a successful deployment (indicated by the script output), we can go to our machine and check container:
```
ssh single-machine@142.93.173.0
docker ps

CONTAINER ID   IMAGE               COMMAND                  CREATED          STATUS          PORTS     NAMES
2d75a904aa0b   single-app:latest   "java -jar single-ap…"   38 seconds ago   Up 38 seconds             single-app
```

We are almost ready to run tests.

## Prepare test data

To make tests realistic, we need to have data in our database. For that, I have prepared a special endpoint in the `single-app`:
```
POST: /accounts/generate-test-data
```

It will insert `1 250 000` random accounts, which can take a few minutes. All we need to do is:
```
ssh single-machine@142.93.173.0
curl -X POST -v http://localhost:80/accounts/generate-test-data

*   Trying 127.0.0.1:80...
* Connected to localhost (127.0.0.1) port 80 (#0)
> POST /accounts/generate-test-data HTTP/1.1
> Host: localhost
> User-Agent: curl/7.81.0
> Accept: */*
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 202 
< Content-Length: 0
< Date: Sat, 21 Dec 2024 10:09:50 GMT
< 
* Connection #0 to host localhost left intact
```

We can then investigate process' progress by checking out `single-app` logs:
```
docker logs single-app

2024-12-21T11:42:26.110Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 10000 accounts were generated, creating them...
2024-12-21T11:42:26.562Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 10000/1250000 accounts were created
2024-12-21T11:42:26.581Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 10000 accounts were generated, creating them...
2024-12-21T11:42:27.491Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 20000/1250000 accounts were created
2024-12-21T11:42:27.500Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 10000 accounts were generated, creating them...
2024-12-21T11:42:28.672Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 30000/1250000 accounts were created
2024-12-21T11:42:28.681Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 10000 accounts were generated, creating them...
2024-12-21T11:42:30.087Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 40000/1250000 accounts were created
2024-12-21T11:42:30.097Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 10000 accounts were generated, creating them...
2024-12-21T11:42:31.901Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 50000/1250000 accounts were created
2024-12-21T11:42:31.913Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 10000 accounts were generated, creating them...
2024-12-21T11:42:33.849Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 60000/1250000 accounts were created
2024-12-21T11:42:33.857Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 10000 accounts were generated, creating them...
...
2024-12-21T11:50:57.459Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 1220000/1250000 accounts were created
2024-12-21T11:50:57.464Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 10000 accounts were generated, creating them...
2024-12-21T11:51:02.456Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 1230000/1250000 accounts were created
2024-12-21T11:51:02.461Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 10000 accounts were generated, creating them...
2024-12-21T11:51:07.449Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 1240000/1250000 accounts were created
2024-12-21T11:51:07.455Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 10000 accounts were generated, creating them...
2024-12-21T11:51:12.496Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 1250000/1250000 accounts were created
2024-12-21T11:51:12.497Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : All accounts were created, it took: PT8M46.477782041S!
```

We can then inspect our db:
```
<copy-paste content of the scripts/build_and_run_sqlite_browser.bash - it will build and run docker container with sqlite3 CLI>
# inside docker

root@e3362a3fb4f0:/db# sqlite3
SQLite version 3.45.1 2024-01-30 16:01:20
Enter ".help" for usage hints.
Connected to a transient in-memory database.
Use ".open FILENAME" to reopen on a persistent database.
sqlite> .open test.db
sqlite> .mode box
sqlite> select count(*) from account;
┌──────────┐
│ count(*) │
├──────────┤
│ 1250002  │
└──────────┘
sqlite> select * from account limit 10;
┌──────────────────────────────────────┬─────────────┬──────────────────────────────────────────────────────┬───────────────┬─────────┐
│                  id                  │    name     │                        email                         │  created_at   │ version │
├──────────────────────────────────────┼─────────────┼──────────────────────────────────────────────────────┼───────────────┼─────────┤
│ 06f40771-6460-479a-a47c-177473e240b5 │ name-76330  │ email-06f40771-6460-479a-a47c-177473e240b5@email.com │ 1734775790016 │ 6704    │
│ 4db7506f-43fe-475e-afbe-842514a6223b │ name-194984 │ email-4db7506f-43fe-475e-afbe-842514a6223b@email.com │ 1734775790019 │ 5802    │
│ 186b6cf7-20ee-4cbb-b567-e4e857d6b2f3 │ name-101133 │ email-186b6cf7-20ee-4cbb-b567-e4e857d6b2f3@email.com │ 1734775790038 │ 6605    │
│ 825a04d7-4d42-4920-9ce8-eee76de5faf8 │ name-110164 │ email-825a04d7-4d42-4920-9ce8-eee76de5faf8@email.com │ 1734775790038 │ 7459    │
│ 902d34c6-9075-4912-8562-437385b88940 │ name-173087 │ email-902d34c6-9075-4912-8562-437385b88940@email.com │ 1734775790038 │ 343     │
│ 091ccb4e-c538-4758-a313-3da462f456e5 │ name-170844 │ email-091ccb4e-c538-4758-a313-3da462f456e5@email.com │ 1734775790038 │ 101     │
│ 34e0f02e-e9ef-4542-9215-37164571d3c1 │ name-155361 │ email-34e0f02e-e9ef-4542-9215-37164571d3c1@email.com │ 1734775790038 │ 337     │
│ a580d0f6-a6d5-4987-b787-2bec8469c569 │ name-81680  │ email-a580d0f6-a6d5-4987-b787-2bec8469c569@email.com │ 1734775790038 │ 6807    │
│ 445a0642-d74d-4476-8775-0d343a0d5074 │ name-241806 │ email-445a0642-d74d-4476-8775-0d343a0d5074@email.com │ 1734775790038 │ 269     │
│ 3bbf6850-ecfe-4f67-b06b-7f10aca4b8d1 │ name-144470 │ email-3bbf6850-ecfe-4f67-b06b-7f10aca4b8d1@email.com │ 1734775790038 │ 946     │
└──────────────────────────────────────┴─────────────┴──────────────────────────────────────────────────────┴───────────────┴─────────┘

# gather table stats for a query planner
sqlite> ANALYZE;
```
 
## Build and deploy load lest

`load-test` is just a custom Java-based load test tool that I have dockerized to make things easier.
Let's build it:
```
bash build_and_package_load_test.bash
```

We have 4 test machines, we need to deploy it to all of them. Of course, it is automated, we just need to grab IP addresses of our machines.
So go to https://cloud.digitalocean.com/droplets again and look out for `test-machine-1`, `test-machine-2`, `test-machine-3` and `test-machine-4`. 
Then, we need to do:
```
export TEST_HOSTS="165.227.153.232 165.232.70.22 68.183.76.205 64.225.109.249"
bash deploy_load_tests.bash
```

It will deploy to 4 test-machines in parallel, but it can still take a while, since these are Docker images, so be patient, we only need to do this once!

## Run Load Test

Finally, we can run those tests!

We have a few options there, but first let's run the basic test:
```
# your single machine ip!
export TESTED_HOST=142.93.173.0
# machine name used in test-results reports
export TESTED_MACHINE_NAME=large
# same as in previous, deploy step
export TEST_HOSTS="165.227.153.232 165.232.70.22 68.183.76.205 64.225.109.249"
bash run_load_test.bash
Running load test on 165.227.153.232 165.232.70.22 68.183.76.205 64.225.109.249 hosts...

Running load test on 165.227.153.232 host with low_load test profile...

load test on 165.227.153.232 is running! To check out results, do (after a few seconds):
ssh test-machine@165.227.153.232 cat /home/test-machine/load-test-results/test_results-large-low_load.txt

Running load test on 165.232.70.22 host with low_load test profile...

load test on 165.232.70.22 is running! To check out results, do (after a few seconds):
ssh test-machine@165.232.70.22 cat /home/test-machine/load-test-results/test_results-large-low_load.txt

Running load test on 68.183.76.205 host with low_load test profile...

load test on 68.183.76.205 is running! To check out results, do (after a few seconds):
ssh test-machine@68.183.76.205 cat /home/test-machine/load-test-results/test_results-large-low_load.txt

Running load test on 64.225.109.249 host with low_load test profile...

load test on 64.225.109.249 is running! To check out results, do (after a few seconds):
ssh test-machine@64.225.109.249 cat /home/test-machine/load-test-results/test_results-large-low_load.txt
```

We can then just use command from the above output and check test results on any of these machines (they take ~ 30 seconds to run):
```
ssh test-machine@64.225.109.249 cat /home/test-machine/load-test-results/test_results-small-average_load.txt

...

3000 requests with 100 per second rate took PT30.317S

...

Tests executed on: 4 machines, in parallel
Executed requests on 1 machine: 3000, with 100/s rate
Requests with connect timeout [5000]: 0, as percentage: 0
Requests with request timeout [5000]: 0, as percentage: 0

Min: 0.001 s
Max: 0.12 s
Mean: 0.005 s

Percentile 10: 0.002 s
Percentile 25: 0.002 s
Percentile 50 (Median): 0.003 s
Percentile 75: 0.006 s
Percentile 90: 0.01 s
Percentile 95: 0.014 s
Percentile 99: 0.025 s
Percentile 99.9: 0.057 s

...

POST: /accounts/execute-random-write
Requests: 306, which is 10% of all requests
Connect timeouts: 0
Request timeouts: 0
Requests by status: {200=306}

...

GET: /accounts/{id}
Requests: 1774, which is 59% of all requests
Connect timeouts: 0
Request timeouts: 0
Requests by status: {404=887, 200=887}

...

GET: /accounts?name={name}
Requests: 920, which is 31% of all requests
Connect timeouts: 0
Request timeouts: 0
Requests by status: {200=920}

...
```

LoadTest supports various test profiles. To load our machine more, we can do:
```
export TEST_PROFILE=high_load
bash run_load_test.bash

...

ssh test-machine@64.225.109.249 cat /home/test-machine/load-test-results/test_results-medium-high_load.txt

...

15000 requests with 500 per second rate took PT30.323S

...

Tests executed on: 4 machines, in parallel
Executed requests on 1 machine: 15000, with 500/s rate
Requests with connect timeout [5000]: 0, as percentage: 0
Requests with request timeout [5000]: 0, as percentage: 0

Min: 0.0 s
Max: 0.194 s
Mean: 0.014 s

Percentile 10: 0.001 s
Percentile 25: 0.001 s
Percentile 50 (Median): 0.005 s
Percentile 75: 0.02 s
Percentile 90: 0.041 s
Percentile 95: 0.051 s
Percentile 99: 0.074 s
Percentile 99.9: 0.107 s

...

POST: /accounts/execute-random-write
Requests: 1479, which is 10% of all requests
Connect timeouts: 0
Request timeouts: 0
Requests by status: {200=1479}

...

GET: /accounts/{id}
Requests: 9056, which is 60% of all requests
Connect timeouts: 0
Request timeouts: 0
Requests by status: {404=4443, 200=4613}

...

GET: /accounts?name={name}
Requests: 4465, which is 30% of all requests
Connect timeouts: 0
Request timeouts: 0
Requests by status: {200=4465}

...
```

Available test profiles:
```
enum TestProfile {
  LOW_LOAD,
  AVERAGE_LOAD,
  HIGH_LOAD, 
  VERY_HIGH_LOAD
}
```
For more options and details, check out `load-test` directory.


## What else you can do

I encourage you to run various test profiles on various machines (small, medium, large and your own) and compare the results!