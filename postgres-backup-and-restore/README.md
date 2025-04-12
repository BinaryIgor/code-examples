# Postgres backup and restore

## Prepare infra
As we often do, let's use DigitalOcean (you can use whatever supports Virtual Machines and something similar to AWS S3 service). From `scripts`, run:
```
bash init_python_env.bash
source venv/bin/activate
export DO_API_TOKEN=<your digital ocean API token>
export SSH_KEY_FINGERPRINT=<your ssh key fingerprint uploaded to DigitalOcean>
python3 prepare_infra.py
```

This will prepare a small droplet, with installed Docker, *deploy user* and some ssh & firewall setup; it should be available after 1 - 3 minutes.

After that, we should be able to grab its ip address from DigitalOcean UI and run:
```
ssh deploy@<your droplet ip>
deploy@postgres-backup-and-restore:~$ docker ps
CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS    PORTS     NAMES
```

It means we can deploy dockerized applications and run examples.

Then, go to: https://cloud.digitalocean.com/spaces; enable Spaces for $5 a month, if you haven't already, and create a bucket in the desired region. Mine is `binaryigor` and I will use it throughout the examples.

## Prepare Postgres

We just need to build a dockerized Postgres package; for that, we will just gzip Postgres Docker image and have a wrapper script that will unzip and load this Docker image on our virtual machine (Droplet). From `scripts`:
```
export APP=postgres-db
bash build_and_package_app.bash
```
In the `dist` directory we now have a few files that together constitute our *deployable package*.

Let's then deploy it (again from `scripts`):
```
export APP=postgres-db
export DEPLOY_HOST=<your droplet ip>
bash deploy_app.bash
```
After a minute or two, it should be up and running:
```
Deploying postgres-db to a deploy@159.223.26.132 host, preparing deploy directories..

Dirs prepared, copying package, this can take a while...
load_and_run_app.bash                                                                                      100%  243     7.8KB/s   00:00    
postgres-db.tar.gz                                                                                         100%  142MB   5.4MB/s   00:26    
run_app.bash                                                                                               100%  499    16.2KB/s   00:00    

Package copied, loading and running app, this can take a while..
Loading postgres-db:latest image, this can take a while...
Loaded image: postgres-db:latest
Image loaded, running it...
Removing previous container....
Error response from daemon: No such container: postgres-db

Starting new postgres-db version...

14d0b6df6f74e0a87c456f1bc0a63ad03f1b39e1b2bddcdf110cd18aabbfc412

App loaded, checking its logs and status after 5s..


PostgreSQL Database directory appears to contain a database; Skipping initialization

2024-04-06 17:26:42.897 UTC [1] LOG:  starting PostgreSQL 16.2 (Debian 16.2-1.pgdg120+2) on x86_64-pc-linux-gnu, compiled by gcc (Debian 12.2.0-14) 12.2.0, 64-bit
2024-04-06 17:26:42.897 UTC [1] LOG:  listening on IPv4 address "0.0.0.0", port 5432
2024-04-06 17:26:42.897 UTC [1] LOG:  listening on IPv6 address "::", port 5432
2024-04-06 17:26:42.900 UTC [1] LOG:  listening on Unix socket "/var/run/postgresql/.s.PGSQL.5432"
2024-04-06 17:26:42.905 UTC [27] LOG:  database system was shut down at 2024-04-06 17:24:57 UTC
2024-04-06 17:26:42.910 UTC [1] LOG:  database system is ready to accept connections

App status:
running
App deployed!
In case of problems you can rollback to previous deployment: /home/deploy/deploy/postgres-db/previous
```

Then run:
```
ssh deploy@<your droplet ip>
docker exec -it postgres-db psql -U backup -d backup_db
```

Copy-paste contents of `generate_db_data.sql`:
```
backup_db=> CREATE TABLE IF NOT EXISTS account (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL
);

CREATE PROCEDURE generate_db_data(batches INTEGER)
LANGUAGE plpgsql
AS
$$
BEGIN
    raise notice 'About to generate data in % batches of 10 000 rows each...', batches;
    FOR i IN 1..batches LOOP
        raise notice 'Executing batch %/%...', i, batches;

        WITH random_uuids AS (
            SELECT generate_series(1, 10000) AS idx, gen_random_uuid() as id
        ),
        random_accounts AS (
            SELECT id, CONCAT('name-', id) AS name, CONCAT('email-', id, '@email.com') AS email
            FROM random_uuids
        )
        INSERT INTO account (id, name, email)
        SELECT id, name, email FROM random_accounts;

    END LOOP;
    raise notice 'All data batches executed!';
END;
$$;
CREATE TABLE
CREATE PROCEDURE
```
We will use this procedure to generate some data to back up; as of now, we don't have anything to back up:
```
backup_db=> \dt
         List of relations
 Schema |  Name   | Type  | Owner  
--------+---------+-------+--------
 public | account | table | backup
(1 row)

backup_db=> select count(*) from account;
 count 
-------
     0
(1 row)

backup_db=> select pg_size_pretty(pg_database_size('backup_db'));
 pg_size_pretty 
----------------
 7516 kB
(1 row)

backup_db=> 
```

Let's change that! For the first round, let's run:
```
backup_db=> call generate_db_data(3);
NOTICE:  About to generate data in 3 batches of 10 000 rows each...
NOTICE:  Executing batch 1/3...
NOTICE:  Executing batch 2/3...
NOTICE:  Executing batch 3/3...
NOTICE:  All data batches executed!
CALL
backup_db=> select count(*) from account;
 count 
-------
 30000
(1 row)

backup_db=> select pg_size_pretty(pg_database_size('backup_db'));
 pg_size_pretty 
----------------
 16 MB
(1 row)
```

Let's have more data:
```
backup_db=> call generate_db_data(100);
NOTICE:  About to generate data in 100 batches of 10 000 rows each...
NOTICE:  Executing batch 1/100...
...
NOTICE:  Executing batch 100/100...
NOTICE:  All data batches executed!
CALL

backup_db=> select count(*) from account;
  count  
---------
 1030000
(1 row)

backup_db=> select pg_size_pretty(pg_database_size('backup_db'));
 pg_size_pretty 
----------------
 286 MB
(1 row)
```

Now, let's back it up!

## Prepare db-backup-job

From scripts:
```
export APP=db-backup-job
bash build_and_package_app.bash
```

In the `dist` dir, we have a generated `.env` file:
```
export DB_NAME=backup_db
export DB_USER=backup
export MAX_LOCAL_BACKUPS=10
export MAX_DO_BACKUPS=50
export UPLOAD_TO_DO_SPACES=true
export DO_REGION=fra1
export DO_SPACES_BUCKET=binaryigor
export DO_SPACES_BUCKET_FOLDER=db-backups
export DO_SPACES_KEY=$(cat /home/deploy/.secrets/do-spaces-key.txt)
export DO_SPACES_SECRET=$(cat /home/deploy/.secrets/do-spaces-secret.txt)
```
Some of it you might want to customize; at the very least, we need to provide `DO_SPACES_KEY` and `DO_SPACES_SECRET` values. To do that, go to DigitalOcean's UI (https://cloud.digitalocean.com/account/api/spaces) and generate new key. `Access Key` is `DO_SPACES_KEY` and `Secret Key` is `DO_SPACES_SECRET`. Once you have it, add them as .txt files on the Droplet, under described above file paths. After that, we have ready to be deployed `db-backup-job`! Let's deploy it by running:
```
export APP=db-backup-job
export DEPLOY_HOST=<your droplet ip>
bash deploy_app.bash
```

Before scheduling it through Cron, let's run it manually (on the Droplet of course):
```
docker start db-backup-job
```

We should see something like this:
```
docker logs db-backup-job

2024-04-07T11:21:02+00:00: about to backup backup_db db with backup user to /backups/backup_20240407_112102.back file...

pg_dump: last built-in OID is 16383
pg_dump: reading extensions
pg_dump: identifying extension members
pg_dump: reading schemas
pg_dump: reading user-defined tables
pg_dump: reading user-defined functions
pg_dump: reading user-defined types
pg_dump: reading procedural languages
pg_dump: reading user-defined aggregate functions
pg_dump: reading user-defined operators
pg_dump: reading user-defined access methods
pg_dump: reading user-defined operator classes
pg_dump: reading user-defined operator families
pg_dump: reading user-defined text search parsers
pg_dump: reading user-defined text search templates
pg_dump: reading user-defined text search dictionaries
pg_dump: reading user-defined text search configurations
pg_dump: reading user-defined foreign-data wrappers
pg_dump: reading user-defined foreign servers
pg_dump: reading default privileges
pg_dump: reading user-defined collations
pg_dump: reading user-defined conversions
pg_dump: reading type casts
pg_dump: reading transforms
pg_dump: reading table inheritance information
pg_dump: reading event triggers
pg_dump: finding extension tables
pg_dump: finding inheritance relationships
pg_dump: reading column info for interesting tables
pg_dump: flagging inherited columns in subtables
pg_dump: reading partitioning data
pg_dump: reading indexes
pg_dump: flagging indexes in partitioned tables
pg_dump: reading extended statistics
pg_dump: reading constraints
pg_dump: reading triggers
pg_dump: reading rewrite rules
pg_dump: reading policies
pg_dump: reading row-level security policies
pg_dump: reading publications
pg_dump: reading publication membership of tables
pg_dump: reading publication membership of schemas
pg_dump: reading subscriptions
pg_dump: reading large objects
pg_dump: reading dependency data
pg_dump: saving encoding = UTF8
pg_dump: saving standard_conforming_strings = on
pg_dump: saving search_path = 
pg_dump: saving database definition
pg_dump: dumping contents of table "public.account"

2024-04-07T11:21:11+00:00: backup done! Checking if we need to remove old backups locally...

No need to remove backups, since we have 1 and 10 are allowed

2024-04-07T11:21:11+00:00: Uploading backup to DO spaces...

Uploading /backups/backup_20240407_112102.back to db-backups/backup_20240407_112102.back on binaryigor space...
Backup uploaded to DO spaces!
Checking if we should remove old ones...
There are less backups (1) than max allowed (50), skipping deletion

2024-04-07T11:21:12+00:00: Backups uploaded to DO spaces!
Backup job is done!
```

Backup should be available both in the local file system (`/home/deploy/db-backups`) as well as on `DO spaces`.

## Schedule db-backup-job

We want to have automatic backups, not manual ones; let's run:
```
bash deploy_crontab.bash
```

We should see something like this:
```
crontab.txt                                                                                                                                         100%   39     1.3KB/s   00:00    
current crontab:
no crontab for deploy

Updating it from /home/deploy/crontab.txt...

Crontab updated, new state:
0 */4 * * * docker start db-backup-job
```
Which means that backup will be done at minute 0 past every 4th hour (more or less every four hours).

## Restore db from a backup

First, let's build `db-restore-job`:
```
export APP=db-restore-job
bash build_and_package_app.bash
```
We also need to deploy it to our Droplet:
```
export APP=db-restore-job
export DEPLOY_HOST=<your droplet ip>
bash deploy_app.bash
```

We don't need to schedule, since it is an on demand task. How it works?

It will connect to the same database as `db-backup-job`.
What's more, it has the same volume attached - `/home/deploy/db-backups:/backups`; as you might remember, `db-backup-job` is storing backups in this directory.
Now, `db-restore-job` is expecting to find a backup named `backup_restore.back`; we just need to put it there. We can either rename one of the local backups or get it from DO spaces, since we also upload them there.

Before doing any of that, let's wipe out the database:
```
docker exec -it postgres-db psql -U backup -d backup_db
psql (16.2 (Debian 16.2-1.pgdg120+2))
Type "help" for help.

backup_db=> drop table account;
DROP TABLE
backup_db=> select pg_size_pretty(pg_database_size('backup_db'));
 pg_size_pretty 
----------------
 7540 kB
(1 row)
```

### Local backup
```
deploy@postgres-backup-and-restore:~/db-backups$ pwd
/home/deploy/db-backups
deploy@postgres-backup-and-restore:~/db-backups$ ls -l
total 960160
-rw-r--r-- 1 root root 98316320 Apr  7 01:00 backup_20240407_010002.back
-rw-r--r-- 1 root root 98316320 Apr  7 02:00 backup_20240407_020001.back
-rw-r--r-- 1 root root 98316320 Apr  7 03:00 backup_20240407_030001.back
-rw-r--r-- 1 root root 98316320 Apr  7 04:00 backup_20240407_040001.back
-rw-r--r-- 1 root root 98316320 Apr  7 05:00 backup_20240407_050001.back
-rw-r--r-- 1 root root 98316320 Apr  7 06:00 backup_20240407_060001.back
-rw-r--r-- 1 root root 98316320 Apr  7 07:00 backup_20240407_070002.back
-rw-r--r-- 1 root root 98316320 Apr  7 08:00 backup_20240407_080001.back
-rw-r--r-- 1 root root 98316320 Apr  7 09:00 backup_20240407_090002.back
-rw-r--r-- 1 root root 98316320 Apr  7 09:05 backup_20240407_090447.back
deploy@postgres-backup-and-restore:~/db-backups$ sudo mv backup_20240407_090447.back backup_restore.back
deploy@postgres-backup-and-restore:~/db-backups$ ls -l
total 960160
-rw-r--r-- 1 root root 98316320 Apr  7 01:00 backup_20240407_010002.back
-rw-r--r-- 1 root root 98316320 Apr  7 02:00 backup_20240407_020001.back
-rw-r--r-- 1 root root 98316320 Apr  7 03:00 backup_20240407_030001.back
-rw-r--r-- 1 root root 98316320 Apr  7 04:00 backup_20240407_040001.back
-rw-r--r-- 1 root root 98316320 Apr  7 05:00 backup_20240407_050001.back
-rw-r--r-- 1 root root 98316320 Apr  7 06:00 backup_20240407_060001.back
-rw-r--r-- 1 root root 98316320 Apr  7 07:00 backup_20240407_070002.back
-rw-r--r-- 1 root root 98316320 Apr  7 08:00 backup_20240407_080001.back
-rw-r--r-- 1 root root 98316320 Apr  7 09:00 backup_20240407_090002.back
-rw-r--r-- 1 root root 98316320 Apr  7 09:05 backup_restore.back
```

Then we just need to run the `db-restore-job`:
```
docker start db-restore-job
```
After a while, we should see something like this, in the logs:
```
2024-04-07T11:42:34+00:00: restoring backup_db db with backup user from /backups/backup_restore.back file...

pg_restore: connecting to database for restore
pg_restore: creating PROCEDURE "public.generate_db_data(integer)"
pg_restore: while PROCESSING TOC:
pg_restore: from TOC entry 216; 1255 16399 PROCEDURE generate_db_data(integer) backup
pg_restore: error: could not execute query: ERROR:  function "generate_db_data" already exists with same argument types
Command was: CREATE PROCEDURE public.generate_db_data(IN batches integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    raise notice 'About to generate data in % batches of 10 000 rows each...', batches;
    FOR i IN 1..batches LOOP
        raise notice 'Executing batch %/%...', i, batches;

        WITH random_uuids AS (
            SELECT generate_series(1, 10000) AS idx, gen_random_uuid() as id
        ),
        random_accounts AS (
            SELECT id, CONCAT('name-', id) AS name, CONCAT('email-', id, '@email.com') AS email
            FROM random_uuids
        )
        INSERT INTO account (id, name, email)
        SELECT id, name, email FROM random_accounts;

    END LOOP;
    raise notice 'All data batches executed!';
END;
$$;


pg_restore: creating TABLE "public.account"
pg_restore: processing data for table "public.account"
pg_restore: creating CONSTRAINT "public.account account_email_key"
pg_restore: creating CONSTRAINT "public.account account_pkey"
pg_restore: creating ACL "SCHEMA public"
pg_restore: WARNING:  no privileges were granted for "public"
pg_restore: warning: errors ignored on restore: 1

2024-04-07T11:42:57+00:00: db restored!
```
As we can see, it didn't take long; let's verify that we have something in the db:
```
docker exec -it postgres-db psql -U backup -d backup_db
psql (16.2 (Debian 16.2-1.pgdg120+2))
Type "help" for help.

backup_db=> select pg_size_pretty(pg_database_size('backup_db'));
 pg_size_pretty 
----------------
 755 MB
(1 row)
```

### Remote backup

Assuming that we have an empty database, go to DigitalOcean UI (spaces) and find your desired backup there. Click quick share - you will be able to generate a temporary url of the kind:
```
https://fra1.digitaloceanspaces.com/binaryigor/db-backups/backup_20240407_090002.back?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=DO00BXFWPDWLPRZQFRNE%2F20240407%2Ffra1%2Fs3%2Faws4_request&X-Amz-Date=20240407T093521Z&X-Amz-Expires=3600&X-Amz-SignedHeaders=host&X-Amz-Signature=5746bc39812706bf6da2f7754c1c81bed75003d5fc98bc4af0df4e6fa4af74c6
```

We can then use it to download this particular backup to our droplet (`db-backup-job/get_backup_from_do_spaces.bash`):
```
deploy@postgres-backup-and-restore:~/db-backups$ pwd
/home/deploy/db-backups

export DO_SPACE_BACKUP_URL="https://fra1.digitaloceanspaces.com/binaryigor/db-backups/backup_20240407_090002.back?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=DO00BXFWPDWLPRZQFRNE%2F20240407%2Ffra1%2Fs3%2Faws4_request&X-Amz-Date=20240407T093521Z&X-Amz-Expires=3600&X-Amz-SignedHeaders=host&X-Amz-Signature=5746bc39812706bf6da2f7754c1c81bed75003d5fc98bc4af0df4e6fa4af74c6"

sudo curl -o "/home/deploy/db-backups/backup_restore.back" ${DO_SPACE_BACKUP_URL}

ls -l

-rw-r--r-- 1 root root 98316320 Apr  7 01:00 backup_20240407_010002.back
-rw-r--r-- 1 root root 98316320 Apr  7 02:00 backup_20240407_020001.back
-rw-r--r-- 1 root root 98316320 Apr  7 03:00 backup_20240407_030001.back
-rw-r--r-- 1 root root 98316320 Apr  7 04:00 backup_20240407_040001.back
-rw-r--r-- 1 root root 98316320 Apr  7 05:00 backup_20240407_050001.back
-rw-r--r-- 1 root root 98316320 Apr  7 06:00 backup_20240407_060001.back
-rw-r--r-- 1 root root 98316320 Apr  7 07:00 backup_20240407_070002.back
-rw-r--r-- 1 root root 98316320 Apr  7 08:00 backup_20240407_080001.back
-rw-r--r-- 1 root root 98316320 Apr  7 09:00 backup_20240407_090002.back
-rw-r--r-- 1 root root 98316320 Apr  7 09:39 backup_restore.back
```

That's it; we can then repeat steps from the previous option:
```
docker start db-restore-job
```
And after a while:
```
docker exec -it postgres-db psql -U backup -d backup_db
psql (16.2 (Debian 16.2-1.pgdg120+2))
Type "help" for help.

backup_db=> select pg_size_pretty(pg_database_size('backup_db'));
 pg_size_pretty 
----------------
 755 MB
(1 row)
```
