# Metrics and Logs Collector

Simple Python app that collects metrics and logs from Docker Containers. 
It then exposes these metrics to Prometheus and saves logs in the local file system, limiting their size and rotating files, if neccessary.

## When to use it

If you have a system that can be deployed on a single machine or a few machines. You don't want to (and shouldn't) bother with Kubernetes and you seek for a simpler solution. In that case you can you just use Docker + a few scripts and tools to make your life easier. This is one of these tools - you only need Docker and a Prometheus instance to use it.


## Overview - how it works

collector.py is an entry point of the application.
When started, it reads config from env variables, which is printed to the console and shown like this:
```
2024-02-11 19:15:47.998 [INFO] collector: Starting collector for local-machine machine!
2024-02-11 19:15:47.998 [INFO] collector: METRICS_COLLECTION_INTERVAL: 20
2024-02-11 19:15:47.998 [INFO] collector: LOGS_COLLECTION_INTERVAL: 5
2024-02-11 19:15:47.998 [INFO] collector: MAX_COLLECTOR_THREADS: 5
2024-02-11 19:15:47.998 [INFO] collector: LAST_METRICS_COLLECTED_AT_FILE: /tmp/last-metrics-collected-at.txt
2024-02-11 19:15:47.998 [INFO] collector: LAST_LOGS_COLLECTED_AT_FILE: /tmp/last-logs-collected-at.txt
2024-02-11 19:15:47.998 [INFO] logs_exporter: LOGS_DIR: /tmp/logs
2024-02-11 19:15:47.998 [INFO] logs_exporter: LOGS_CONTAINER_MAX_FILES: 10
2024-02-11 19:15:47.998 [INFO] logs_exporter: LOGS_CONTAINER_MAX_FILE_SIZE: 10485760
2024-02-11 19:15:47.998 [INFO] logs_exporter: LOG_LEVELS_MAPPING_PATH: /config/log_levels_mapping.json

2024-02-11 19:15:47.998 [INFO] collector: Trying to get client...
2024-02-11 19:15:48.011 [INFO] collector: Client connected, docker version: {
  "Platform": {
    "Name": "Docker Engine - Community"
  },
  "Components": [
    {
      "Name": "Engine",
      "Version": "25.0.3",
      "Details": {
        "ApiVersion": "1.44",
        "Arch": "amd64",
        "BuildTime": "2024-02-06T21:14:17.000000000+00:00",
        "Experimental": "false",
        "GitCommit": "f417435",
        "GoVersion": "go1.21.6",
        "KernelVersion": "6.5.0-17-generic",
        "MinAPIVersion": "1.24",
        "Os": "linux"
      }
    },
    {
      "Name": "containerd",
      "Version": "1.6.28",
      "Details": {
        "GitCommit": "ae07eda36dd25f8a1b98dfbf587313b99c0190bb"
      }
    },
    {
      "Name": "runc",
      "Version": "1.1.12",
      "Details": {
        "GitCommit": "v1.1.12-0-g51d5e94"
      }
    },
    {
      "Name": "docker-init",
      "Version": "0.19.0",
      "Details": {
        "GitCommit": "de40ad0"
      }
    }
  ],
  "Version": "25.0.3",
  "ApiVersion": "1.44",
  "MinAPIVersion": "1.24",
  "GitCommit": "f417435",
  "GoVersion": "go1.21.6",
  "Os": "linux",
  "Arch": "amd64",
  "KernelVersion": "6.5.0-17-generic",
  "BuildTime": "2024-02-06T21:14:17.000000000+00:00"
}

2024-02-11 19:15:48.012 [INFO] collector: Metrics are exported on port 10101
```

It then tries to connect to the Docker Engine, retrying as many times as needed.

Then the flow continues as follows in the infinite loop:
* get running containers from `Containers` class
* collect metrics if needed, according to *METRICS_COLLECTION_INTERVAL*, using *MAX_COLLECTOR_THREADS* to make it faster
* if new metrics were collected:
    * update metrics in `metrics_exporter.py` so that Prometheus can scrape up-to-date values
    * update `LAST_METRICS_COLLECTED_AT_FILE` with new timestamp value
* collect logs if needed, according to *LOGS_COLLECTION_INTERVAL*
* if new logs were collected:
    * log logs of every container in `logs_exporter.py` using standard Python `RotatingFileHandler` class
    * export logs metrics using appriopriate log_levels_mapping
    * above mechanism allows to define custom logs level assignment logic according to ones defined in a file - for details check `src/config/log_levels_mapping.json` and `logs_exporter.export()` function
    * update `LAST_LOGS_COLLECTED_AT_FILE` with new timestamp value
* sleep for `min(METRICS_COLLECTION_INTERVAL, LOGS_COLLECTION_INTERVAL)` and then repeat the whole process


## Examples/how to tinker with it

All you need is an ability to run Docker and most likely Linux-based system (might work on others also, but it is not guaranteed).

From containers/ dir run:
```
bash start_all_containers.bash
```

After a while, which can take some time - Docker might need to pull multiple base images from the net, you should run:
```
docker ps
```
and see:
```
CONTAINER ID   IMAGE             COMMAND                  CREATED              STATUS              PORTS                                                 NAMES
0258d678d22c   logs-browser      "/docker-entrypoint.…"   58 seconds ago       Up 58 seconds       80/tcp, 0.0.0.0:11111->8080/tcp, :::11111->8080/tcp   logs-browser
7e205caec5d0   some-custom-app   "python3 -u some_cus…"   About a minute ago   Up About a minute                                                         some-custom-app
f32f1e89a9ee   prometheus        "/bin/prometheus --s…"   About a minute ago   Up About a minute                                                         prometheus
4497cde6e2a5   postgres-db       "docker-entrypoint.s…"   About a minute ago   Up About a minute   0.0.0.0:5432->5432/tcp, :::5432->5432/tcp             postgres-db

```

Just a few containers to play with. 

### Prometheus

We will expose metrics to Prometheus, so after running above commands, you should be able to go to http://localhost:9090 and see Prometheus UI. From that place we can check metrics and alerts. As a reference, go to http://localhost:9090/graph and run query:
```
{__name__=~"container.+"}
```

It should give empty results for now, but most of the exposed by the tool metrics will be available here.

### Browsing logs

Logs are saved by the collector the local file system, by default under `/tmp/metrics-and-logs-collector/logs` path. After running above command, it should be empty, as we haven't started `collector` yet.  

Additionaly, we have `logs-browser` container: it is just an nginx instance configured for browsing static files. At this point, you should go to http://localhost:11111 and see empty list. Later on, we will see logs here.

### Collector

At this point, we know how *collector* works and where we should expect metrics and logs, collected by it. Let's then start it! From the root folder (metrics-and-logs-collector) run:
```
bash build_and_run_collector.bash
```
After a while, you should run:
```
docker ps
```
and see:
```
CONTAINER ID   IMAGE                        COMMAND                  CREATED          STATUS          PORTS                                                 NAMES
6e9142386269   metrics-and-logs-collector   "python3 -u collecto…"   5 seconds ago    Up 4 seconds    0.0.0.0:10101->10101/tcp, :::10101->10101/tcp         metrics-and-logs-collector
b05a6b3d0fb9   logs-browser                 "/docker-entrypoint.…"   13 minutes ago   Up 13 minutes   80/tcp, 0.0.0.0:11111->8080/tcp, :::11111->8080/tcp   logs-browser
45a3639edd39   some-custom-app              "python3 -u some_cus…"   13 minutes ago   Up 13 minutes                                                         some-custom-app
df478cc3dbb4   prometheus                   "/bin/prometheus --s…"   13 minutes ago   Up 13 minutes                                                         prometheus
5013fb6189b4   postgres-db                  "docker-entrypoint.s…"   13 minutes ago   Up 13 minutes   0.0.0.0:5432->5432/tcp, :::5432->5432/tcp             postgres-db
```

If you are curious you can run:
```
docker logs metrics-and-logs-collector
```
And see...

## Useful

Get all Prometheus metrics:
```
{__name__=~".+"}
```

## TODO
* In theory, Podman is compatible with Docker, so it should also work there (maybe with minor modifications)