import json
import time

from prometheus_client import start_http_server, Gauge


class ContainerMetrics:

    def __init__(self, container, started_at, used_memory, max_memory, cpu_usage):
        self.container = container
        self.started_at = started_at
        self.used_memory = used_memory
        self.max_memory = max_memory
        self.cpu_usage = cpu_usage


# TODO: additional labels

MACHINE_LABEL = "machine"
CONTAINER_LABEL = "container"

COMMONS_LABELS = [MACHINE_LABEL, CONTAINER_LABEL]

collector_up_timestamp_gauge = Gauge("collector_up_timestamp_seconds", "TODO", [MACHINE_LABEL])


def export(port):
    start_http_server(port)


def on_next_collection(machine):
    collector_up_timestamp_gauge.labels(machine=machine).set(int(time.time()))


def on_new_container_metrics(machine, metrics):
    print(f"Machine {machine}, container metrics: {metrics.__dict__}")
