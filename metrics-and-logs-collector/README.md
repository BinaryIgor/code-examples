# Metrics and Logs Collector

Simple Python app/tool that collects metrics and logs from Docker Containers. 

It then exposes those metrics to Prometheus (which just means having an http endpoint) and saves logs in the local file system, limiting their size and rotating files, if necessary (it's also pretty straightforward to save logs somewhere else, in a database for example).

Basic requirements:
* Python 3.10, compatible pip & deps specified in src/requirements.txt
* Alternatively just Docker and running it as a container - see *build_and_run_collector.bash*
* Prometheus, if you want to make practical use of the exposed metrics

## When to use it

If you work on a system that can be deployed on a single machine or a few machines. If this is the case, you don't want to bother with Kubernetes complexity, so you seek for a simpler solution. You can just use Docker and a few scripts and tools to make your life easier. This is one of those tools - only Docker and a Prometheus instance is needed to use it.

## How it works

*collector.py* is an entry point of the *metrics-and-logs-collector* tool.
When started, it reads config from env variables and prints it to the console like this:
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

After this warm welcome, it tries to connect to the Docker Engine, retrying as many times as needed.

Then, the flow continues in the following, infinite loop:
* get *running* containers from `Containers` class
* if needed, collect metrics according to `METRICS_COLLECTION_INTERVAL`, using `MAX_COLLECTOR_THREADS` to make it faster
* if new metrics were collected:
    * update metrics in *metrics_exporter.py* so that Prometheus can scrape up-to-date values
    * update `LAST_METRICS_COLLECTED_AT_FILE` with new timestamp value
* collect logs:
    * log logs of every container in *logs_exporter.py* using standard Python `RotatingFileHandler` class
    * export logs metrics using appropriate *log_levels_mapping*
    * *log_levels_mapping* mechanism allows to define custom logs level assignment logic according to a chosen config file - for details check *src/config/log_levels_mapping.json* and `logs_exporter.export(machine, container_logs)` function
    * update `LAST_LOGS_COLLECTED_AT_FILE` with new timestamp value
* sleep for `LOGS_COLLECTION_INTERVAL` (it is always <= `METRICS_COLLECTION_INTERVAL`) and then repeat the whole process again, as long as the program is alive


## How to tinker and experiment with it

All you need is an ability to run Docker and most likely Linux-based system (might work on others also, but it is not guaranteed).

From `containers` directory run:
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

Logs are saved by the *collector* in the local file system, by default under `/tmp/metrics-and-logs-collector/logs` path. As of now, it should be empty, as we haven't started the *collector* yet.  

Additionally, we have *logs-browser* container: it is just a Nginx instance configured for browsing static files. 
At this point, you should go to http://localhost:11111 and see empty list. Later on, we will see logs from containers here.

### Collector

As of now, we know how *collector* works and where we should expect containers metrics and logs, collected by it. Let's then start the *collector*! 
From the root folder (*metrics-and-logs-collector*) run:
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

If you are curious, you can run:
```
docker logs metrics-and-logs-collector
```
And see something similar to:
```
...

2024-02-12 17:14:53.070 [INFO] collector: Checking containers...
2024-02-12 17:14:53.082 [INFO] collector: To check containers: ['metrics-and-logs-collector: 4fa4be087200b854aa02a40212b7e1f0ea96d9662d489d9e76d4114be84a9cc2', 'logs-browser: c9082e429507c23252b41abee694b30584e1e2013a2817e04860aa955458af07', 'some-custom-app: a9aea1bc4420a5d0002e6246b218db1dc2e16cfa9ef0dd967317a1c23a8f0268', 'prometheus: 8072263ab5373dc3269d561b4349734ac2a9782a44e0c5f575d54f357a1de4d7', 'postgres-db: e9208517aaacbaef2bbc7edb6a40058051ca367e7f4bf26931e7c85774e2bec9']
2024-02-12 17:14:53.082 [INFO] containers: Have 5 running containers, checking their metrics/stats...
2024-02-12 17:14:55.103 [INFO] containers: 
Metrics checked.

2024-02-12 17:14:55.103 [INFO] collector: Updating last-data-read-at file: /tmp/last-metrics-collected-at.txt
2024-02-12 17:14:55.103 [INFO] containers: Have 5 running containers, checking their logs...
2024-02-12 17:14:55.134 [INFO] containers: 
Logs checked.

2024-02-12 17:14:55.134 [INFO] collector: Updating last-data-read-at file: /tmp/last-logs-collected-at.txt
2024-02-12 17:14:55.135 [INFO] collector: 
Sleeping for 5s...

```
...which means that the *collector* is running and collecting. 

Running Prometheus query (http://localhost:9090):
```
{__name__=~"container.*"}
```
should give you loads of metrics like these:
```
container_cpu_usage_percent{container="logs-browser", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
0
container_cpu_usage_percent{container="metrics-and-logs-collector", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
0.0039
container_cpu_usage_percent{container="postgres-db", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
0
container_cpu_usage_percent{container="prometheus", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
0.0001
container_cpu_usage_percent{container="some-custom-app", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
2.0063
container_cpus_available{container="logs-browser", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
0.25
container_cpus_available{container="metrics-and-logs-collector", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
0.5
container_cpus_available{container="postgres-db", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
1
container_cpus_available{container="prometheus", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
1
container_cpus_available{container="some-custom-app", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
12
container_logs_created{container="metrics-and-logs-collector", instance="localhost:10101", job="metrics-and-logs-collector", level="info", machine="local-machine"}
1707758095.1165588
container_logs_created{container="postgres-db", instance="localhost:10101", job="metrics-and-logs-collector", level="info", machine="local-machine"}
1707758117.280608
container_logs_created{container="some-custom-app", instance="localhost:10101", job="metrics-and-logs-collector", level="error", machine="local-machine"}
1707758095.1268246
container_logs_created{container="some-custom-app", instance="localhost:10101", job="metrics-and-logs-collector", level="info", machine="local-machine"}
1707758100.168529
container_logs_created{container="some-custom-app", instance="localhost:10101", job="metrics-and-logs-collector", level="warning", machine="local-machine"}
1707758161.573784
container_logs_total{container="metrics-and-logs-collector", instance="localhost:10101", job="metrics-and-logs-collector", level="info", machine="local-machine"}
21
container_logs_total{container="postgres-db", instance="localhost:10101", job="metrics-and-logs-collector", level="info", machine="local-machine"}
1
container_logs_total{container="some-custom-app", instance="localhost:10101", job="metrics-and-logs-collector", level="error", machine="local-machine"}
13
container_logs_total{container="some-custom-app", instance="localhost:10101", job="metrics-and-logs-collector", level="info", machine="local-machine"}
7
container_logs_total{container="some-custom-app", instance="localhost:10101", job="metrics-and-logs-collector", level="warning", machine="local-machine"}
1
container_max_memory_bytes{container="logs-browser", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
262144000
container_max_memory_bytes{container="metrics-and-logs-collector", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
262144000
container_max_memory_bytes{container="postgres-db", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
524288000
container_max_memory_bytes{container="prometheus", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
262144000
container_max_memory_bytes{container="some-custom-app", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
33561669632
container_started_at_timestamp_seconds{container="logs-browser", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
1707757813
container_started_at_timestamp_seconds{container="metrics-and-logs-collector", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
1707758092
container_started_at_timestamp_seconds{container="postgres-db", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
1707757808
container_started_at_timestamp_seconds{container="prometheus", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
1707757810
container_started_at_timestamp_seconds{container="some-custom-app", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
1707757812
container_up_timestamp_seconds{container="logs-browser", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
1707758205
container_up_timestamp_seconds{container="metrics-and-logs-collector", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
1707758204
container_up_timestamp_seconds{container="postgres-db", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
1707758205
container_up_timestamp_seconds{container="prometheus", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
1707758205
container_up_timestamp_seconds{container="some-custom-app", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
1707758205
container_used_memory_bytes{container="logs-browser", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
20320256
container_used_memory_bytes{container="metrics-and-logs-collector", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
31956992
container_used_memory_bytes{container="postgres-db", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
115363840
container_used_memory_bytes{container="prometheus", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
104755200
container_used_memory_bytes{container="some-custom-app", instance="localhost:10101", job="metrics-and-logs-collector", machine="local-machine"}
26755072
```

Additionally, all Prometheus alerts should be soon off, which you can see by clicking *Alerts* on the Prometheus UI. 
If you are curious, whole Prometheus config is available under `containers/prometheus` directory.

At this point, we should also have access to logs. 
Some of the containers log messages only on start, so let's stop them by running:
```
bash stop_all_containers.bash
```
from `containers` directory. Let's then run again:
```
bash start_all_containers.bash
```

As previously, go to `/tmp/metrics-and-logs-collector/logs` and run:
```
ls -l
```
you should see something like:
```
drwxr-xr-x 2 root root 4096 lut 12 18:25 logs-browser
drwxr-xr-x 2 root root 4096 lut 12 18:14 metrics-and-logs-collector
drwxr-xr-x 2 root root 4096 lut 12 18:15 postgres-db
drwxr-xr-x 2 root root 4096 lut 12 18:25 prometheus
drwxr-xr-x 2 root root 4096 lut 12 18:14 some-custom-app
```
where in each folder we have logs from a container. 
You can also access them by going to the browser: http://localhost:11111, thanks to our *logs-browser* container.

We can also run:
```
docker stats
```
to see stats of various containers:
```
CONTAINER ID   NAME                         CPU %     MEM USAGE / LIMIT     MEM %     NET I/O         BLOCK I/O         PIDS
4fa4be087200   metrics-and-logs-collector   0.01%     26.23MiB / 250MiB     10.49%    51kB / 70.3kB   10.3MB / 2.17MB   7
2f9f1945e109   postgres-db                  0.01%     20.75MiB / 500MiB     4.15%     6.39kB / 0B     455kB / 40MB      6
711f8b2caa5c   prometheus                   0.00%     27.3MiB / 250MiB      10.92%    0B / 0B         1.29MB / 193kB    18
2b5a090a92a2   some-custom-app              300.12%   16.98MiB / 31.26GiB   0.05%     6.32kB / 0B     0B / 1.17MB       6
646b60631e94   logs-browser                 0.00%     9.902MiB / 250MiB     3.96%     5.89kB / 0B     0B / 8.19kB       13
```
What's worth noting is that *metrics-and-logs-collector* keeps its CPU and MEM usage extremely low on all times :)