# What a Single Machine can handle?

I often talk about simple architectures and how monoliths should be preferred to distributed services.
In this context, let's see how much load, http requests/second to be exact, can a Single Machine handle.

## What are we going to do?

Execute LoadTest against single machine with basic Java 21 rest service and Postgresql db.

We will test single machines with various resources (memory and cpu):
* 1 CPU, 2 GB of memory
* 2 CPUs, 4 GB of memory
* 4 CPUs, 8 GB of memory

Tests, http requests, will be executed from a few machines (4), in parallel.

In the database, we will have a little over 1 million random rows of the table (initialized by `single-app`:
```
CREATE TABLE account (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL
);

CREATE INDEX account_name ON account(name);
```

What's more, we will have realistic endpoints:
```
GET: /accounts/{id}
GET: /accounts/count?name={name}
# randomly executes either insert or delete
POST: /accounts/execute-random-write
```

All of that, to answer the question:
> How many http requests per second can a Single Machine handle?

## What we have here?

* single-app: simple Java 21 + Virtual Threads + Spring Boot 3 application
* single-db: Postgres db with one table having a little over one million rows
* load-test: script to execute various loads of http requests and output detailed results
* scripts to automate operations:
    * creating infrastructure on DigitalOcean from scratch: single-machine, db volume and 4 test machines, where we will
      execute `load-test`
    * building and deploying our apps: single-app, single-db and load-test
    * executing `load-test` on test machines + generating test reports

## Some requirements

* DigitalOcean account (or an API token of someone's else account) - we need to create machines there
* In theory, we just need to have virtual machine for `single-app` and `single-db` + a few virtual machines for `load-test`,
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
  Postgres database

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
* small - 1 CPU + 2 GB memory droplet
* medium - 2 CPUs + 4 GB memory droplet
* small - 4 CPUs + 8 GB memory droplet with dedicated, not shared, cpu(s)

This script can take a while to finish, because it also creates 4 test droplets - we will run load tests from them - and volume for the Postgres db.
Additionally, it is idempotent - we can run it multiple times, and it will figure out what needs to created and what
needs to skipped - it just doesn't do updates, only creates and deletes.

After completion, it can take up to 3 - 5 minutes for machines to be fully ready, so be patient ;)

## Build and deploy single app and db

### Build

Once the infra is ready, we can start experimenting. First, we need to build db and app packages. Fortunately, it needs to be done only once.
From `scripts` dir, run:
```
bash build_and_package_apps.bash
```
This will build and package Postgres db and our Spring Boot app. 
Running it for the first time can take a few minutes, since we need to download a few Docker images + Maven dependencies for the Java app.
Fortunately, it only needs to be done once and consequent builds will be significantly faster, in case of changes.

At this point, in `single-db/dist` and `single-app/dist` we should have ready to be deployed packages like this:
```
ls -l
total 195676
-rw-rw-r-- 1 igor igor       241 mar 23 09:31 load_and_run_app.bash
-rw-rw-r-- 1 igor igor       436 mar 23 09:31 run_app.bash
-rw-rw-r-- 1 igor igor 200359093 mar 23 09:31 single-app.tar.gz

ls -l
total 141648
-rw-rw-r-- 1 igor igor       239 mar 23 09:30 load_and_run_app.bash
-rw-rw-r-- 1 igor igor       488 mar 23 09:30 run_app.bash
-rw-rw-r-- 1 igor igor 145034285 mar 23 09:30 single-db.tar.gz
```

These are just gzipped docker images that we will deploy to our `single-machine`.

### Deploy

We have our packages ready, let's then deploy them!

From the DigitalOcean UI (https://cloud.digitalocean.com/droplets) we can grab the IP address of our droplet.
Let's say that it is `142.93.173.0`. We can now run deploy scripts:
```
# remember to change deploy host to your ip address!
export DEPLOY_HOST=142.93.173.0
bash deploy_apps.bash
```

This will use `ssh` and `scp` to copy previously built packages and deploy them to our glorious Single Machine.
Images weigh a few hundreds MB, so it can take a while.

After a successful deployment (indicated by the script output), we can go to our machine and check containers:
```
ssh single-machine@142.93.173.0
docker ps
CONTAINER ID   IMAGE               COMMAND                  CREATED              STATUS              PORTS     NAMES
fbcc9d7b033e   single-app:latest   "/bin/sh -c 'exec ja…"   About a minute ago   Up About a minute             single-app
a07cbbc7d795   single-db:latest    "docker-entrypoint.s…"   2 minutes ago        Up 2 minutes                  single-db
```

We are almost ready to run tests.

## Prepare test data

To make test realistic, we need to have data in our database. For that, I have prepared special endpoint in `single-app`:
```
POST: /accounts/generate-test-data
```

It will insert `1 250 000` random accounts, which can take up to 30 - 60 seconds. All we need to do is:
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
< Date: Sat, 23 Mar 2024 08:46:03 GMT
< 
* Connection #0 to host localhost left intact
```

We can then investigate process' progress by checking out `single-app` logs:
```
docker logs single-app

2024-03-23T08:46:03.821Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:05.061Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 50000/1250000 accounts were created
2024-03-23T08:46:05.093Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:06.333Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 100000/1250000 accounts were created
2024-03-23T08:46:06.379Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:07.664Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 150000/1250000 accounts were created
2024-03-23T08:46:07.713Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:09.109Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 200000/1250000 accounts were created
2024-03-23T08:46:09.143Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:10.550Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 250000/1250000 accounts were created
2024-03-23T08:46:10.596Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:12.002Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 300000/1250000 accounts were created
2024-03-23T08:46:12.029Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:13.396Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 350000/1250000 accounts were created
2024-03-23T08:46:13.430Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:14.832Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 400000/1250000 accounts were created
2024-03-23T08:46:14.867Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:16.217Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 450000/1250000 accounts were created
2024-03-23T08:46:16.243Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:17.680Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 500000/1250000 accounts were created
2024-03-23T08:46:17.707Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:19.131Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 550000/1250000 accounts were created
2024-03-23T08:46:19.160Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:20.569Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 600000/1250000 accounts were created
2024-03-23T08:46:20.600Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:22.048Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 650000/1250000 accounts were created
2024-03-23T08:46:22.075Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:23.552Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 700000/1250000 accounts were created
2024-03-23T08:46:23.578Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:25.089Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 750000/1250000 accounts were created
2024-03-23T08:46:25.132Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:26.599Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 800000/1250000 accounts were created
2024-03-23T08:46:26.625Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:28.074Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 850000/1250000 accounts were created
2024-03-23T08:46:28.099Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:29.537Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 900000/1250000 accounts were created
2024-03-23T08:46:29.581Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:31.088Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 950000/1250000 accounts were created
2024-03-23T08:46:31.114Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:32.711Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 1000000/1250000 accounts were created
2024-03-23T08:46:32.738Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:34.255Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 1050000/1250000 accounts were created
2024-03-23T08:46:34.298Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:36.228Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 1100000/1250000 accounts were created
2024-03-23T08:46:36.254Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:38.500Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 1150000/1250000 accounts were created
2024-03-23T08:46:38.525Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:40.266Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 1200000/1250000 accounts were created
2024-03-23T08:46:40.326Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : Next 50000 accounts were generated, creating them...
2024-03-23T08:46:41.901Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : 1250000/1250000 accounts were created
2024-03-23T08:46:41.901Z  INFO 1 --- [single-app] [               ] c.b.s.app.account.AccountController      : All accounts were created, it took: PT38.181592545S!
```

We can then inspect our db:
```
docker exec -it single-db psql -U postgres -d single_db
psql (15.3 (Debian 15.3-1.pgdg120+1))
Type "help" for help.

single_db=# select count(*) from account;
  count  
---------
 1250002
(1 row)

single_db=# select * from account limit 10;
                  id                  |   name   |                        email                         |         created_at         | version 
--------------------------------------+----------+------------------------------------------------------+----------------------------+---------
 06f40771-6460-479a-a47c-177473e240b5 | name-114 | email-06f40771-6460-479a-a47c-177473e240b5@email.com | 2024-03-23 10:53:28.017766 |    6738
 4db7506f-43fe-475e-afbe-842514a6223b | name-286 | email-4db7506f-43fe-475e-afbe-842514a6223b@email.com | 2024-03-23 10:53:28.017766 |    9991
 2d16bc69-5123-4d64-90c6-034e02f80861 | name-308 | email-2d16bc69-5123-4d64-90c6-034e02f80861@email.com | 2024-03-23 10:53:28.766826 |    6799
 30735615-3de6-4689-8e69-438494d671bd | name-601 | email-30735615-3de6-4689-8e69-438494d671bd@email.com | 2024-03-23 10:53:28.766826 |    4430
 cf4d1218-bfc2-49e9-96be-5c46fc6ccaf4 | name-500 | email-cf4d1218-bfc2-49e9-96be-5c46fc6ccaf4@email.com | 2024-03-23 10:53:28.766826 |    5687
 d33f0717-2a6c-4523-a363-d75d9441dc09 | name-117 | email-d33f0717-2a6c-4523-a363-d75d9441dc09@email.com | 2024-03-23 10:53:28.766826 |     379
 0f836db5-3911-47b9-bab6-3139ae1de38b | name-60  | email-0f836db5-3911-47b9-bab6-3139ae1de38b@email.com | 2024-03-23 10:53:28.766826 |    1933
 4b97938d-c907-47fc-83ba-fba797e7c33b | name-302 | email-4b97938d-c907-47fc-83ba-fba797e7c33b@email.com | 2024-03-23 10:53:28.766826 |    2026
 0e1979c7-9411-4acc-86fb-997203ef12c1 | name-272 | email-0e1979c7-9411-4acc-86fb-997203ef12c1@email.com | 2024-03-23 10:53:28.766826 |    3070
 246935b2-8add-4677-b99a-53abe9d1fa32 | name-597 | email-246935b2-8add-4677-b99a-53abe9d1fa32@email.com | 2024-03-23 10:53:28.766826 |    3674
(10 rows)

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

We can then just use command from the above output and check test results on any of these machines (they take ~ 15 seconds to run):
```
ssh test-machine@64.225.109.249 cat /home/test-machine/load-test-results/test_results-large-low_load.txt

...

75 requests with 5 per second rate, issued on 4 machines, took PT15.207S

...

Tests executed on: 4 machines, in parallel
Executed requests: 75, with 5/s rate
Requests with connect timeout [5000]: 0, as percentage: 0
Requests with request timeout [5000]: 0, as percentage: 0

Min: 0.002 s
Max: 0.072 s
Mean: 0.005 s

Percentile 10: 0.003 s
Percentile 25: 0.004 s
Percentile 50 (Median): 0.004 s
Percentile 75: 0.005 s
Percentile 90: 0.006 s
Percentile 95: 0.006 s
Percentile 99: 0.072 s
Percentile 999: 0.072 s

...

POST: /accounts/execute-random-write
Requests: 19, which is 25% of all requests
Connect timeouts: 0
Request timeouts: 0
Requests by status: {200=19}

...

GET: /accounts/{id}
Requests: 33, which is 44% of all requests
Connect timeouts: 0
Request timeouts: 0
Requests by status: {404=15, 200=18}

...

GET: /accounts/count?name={name}
Requests: 23, which is 31% of all requests
Connect timeouts: 0
Request timeouts: 0
Requests by status: {200=23}

...
```

LoadTest supports various test profiles. To load our machine more, we can do:
```
export TEST_PROFILE=very_high_load
bash run_load_test.bash

...

ssh test-machine@64.225.109.249 cat /home/test-machine/load-test-results/test_results-large-very_high_load.txt

...

15000 requests with 1000 per second rate, issued on 4 machines, took PT15.362S

...

Tests executed on: 4 machines, in parallel
Executed requests: 15000, with 1000/s rate
Requests with connect timeout [5000]: 0, as percentage: 0
Requests with request timeout [5000]: 0, as percentage: 0

Min: 0.0 s
Max: 1.373 s
Mean: 0.076 s

Percentile 10: 0.001 s
Percentile 25: 0.001 s
Percentile 50 (Median): 0.002 s
Percentile 75: 0.003 s
Percentile 90: 0.25 s
Percentile 95: 0.706 s
Percentile 99: 0.944 s
Percentile 999: 1.165 s

...

POST: /accounts/execute-random-write
Requests: 2898, which is 19% of all requests
Connect timeouts: 0
Request timeouts: 0
Requests by status: {200=2898}

...

GET: /accounts/{id}
Requests: 6124, which is 41% of all requests
Connect timeouts: 0
Request timeouts: 0
Requests by status: {404=3100, 200=3024}

...

GET: /accounts/count?name={name}
Requests: 5978, which is 40% of all requests
Connect timeouts: 0
Request timeouts: 0
Requests by status: {200=5978}

...
```

Available test profiles:
```
enum TestProfile {
  LOW_LOAD, AVERAGE_LOAD, HIGH_LOAD, VERY_HIGH_LOAD
}
```
For more options and details, check out `load-test` directory.


## What else you can do

I encourage you to run various test profiles on various machines (small, medium, large and your own) and compare the results!


