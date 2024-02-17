from prometheus_client import start_http_server, Gauge, Counter

import utils


class ContainerMetrics:

    def __init__(self, container, started_at, used_memory, max_memory, cpu_usage, cpus_available):
        self.container = container
        self.started_at = started_at
        self.used_memory = used_memory
        self.max_memory = max_memory
        self.cpu_usage = cpu_usage
        self.cpus_available = cpus_available


MACHINE_LABEL = "machine"
CONTAINER_LABEL = "container"

COMMONS_LABELS = [MACHINE_LABEL, CONTAINER_LABEL]

collector_up_timestamp_gauge = Gauge("collector_up_timestamp_seconds",
                                     "Heartbeat of the collector, increased after every metrics/logs collection",
                                     [MACHINE_LABEL])

container_started_at_timestamp_gauge = Gauge("container_started_at_timestamp_seconds",
                                             "When container has started",
                                             COMMONS_LABELS)
container_up_timestamp_gauge = Gauge("container_up_timestamp_seconds",
                                     "Heartbeat of a container, increased after every metrics collection",
                                     COMMONS_LABELS)
container_used_memory_gauge = Gauge("container_used_memory_bytes",
                                    "Current container memory usage in bytes",
                                    COMMONS_LABELS)
container_max_memory_gauge = Gauge("container_max_memory_bytes",
                                   "Current container memory limit. "
                                   "If a container is not limited, it's equal to the host memory",
                                   COMMONS_LABELS)
container_cpu_usage_gauge = Gauge("container_cpu_usage_percent",
                                  "Current container cpu usage of the host resources (cpu). "
                                  "It doesn't take into account how many cpus are available for the container, "
                                  "so remember to take container_cpus_available into account as well. "
                                  "Additionally, remember that a host with 1 cpu can have up to 100% cpu usage, "
                                  "but one with 4 can have 400%! "
                                  "This is an important detail when designing alerts",
                                  COMMONS_LABELS)
container_cpus_available = Gauge("container_cpus_available",
                                 "How many cpus are available for the container. "
                                 "If it doesn't have limits there, it's equal to the host cpus. "
                                 "Keep in mind that it's a floating point number and it can be less than 1.0",
                                 COMMONS_LABELS)

container_logs_total = Counter("container_logs_total",
                               "Count of container logs collection with appropriate level classification",
                               COMMONS_LABELS + ["level"])


def export(port):
    start_http_server(port)


def on_next_collection(machine):
    collector_up_timestamp_gauge.labels(machine=machine).set(utils.current_timestamp())


def on_new_container_metrics(machine, metrics):
    container = metrics.container

    container_started_at_timestamp_gauge.labels(machine=machine, container=container).set(metrics.started_at)

    container_up_timestamp_gauge.labels(machine=machine, container=container).set(utils.current_timestamp())

    container_used_memory_gauge.labels(machine=machine, container=container).set(metrics.used_memory)
    container_max_memory_gauge.labels(machine=machine, container=container).set(metrics.max_memory)

    container_cpu_usage_gauge.labels(machine=machine, container=container).set(metrics.cpu_usage)
    container_cpus_available.labels(machine=machine, container=container).set(metrics.cpus_available)


def on_new_container_logs(machine, container, level):
    container_logs_total.labels(machine=machine, container=container, level=level).inc()
