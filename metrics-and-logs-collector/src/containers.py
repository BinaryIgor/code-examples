import json
import re
from datetime import datetime
from os import environ

import docker
from docker.errors import NotFound

import logs_exporter
import metrics_exporter
import utils

DEFAULT_LOGS_SINCE_NOW = int(environ.get("DEFAULT_LOGS_SINCE_NOW", 60))
CPU_NANOS = 1_000_000_000.0

logger = utils.new_logger("containers")


class Containers:
    """
    Wrapper class for containers states.
    We need previous_containers field to make sure that we collect metrics & logs from containers that died after previous collection,
    but before the next one
    """

    def __init__(self, client):
        self.client = client
        self.previous_containers = {}
        self.containers = {}
        self.container_names = []
        self.containers_last_logs_check_timestamps = {}

    def fetch(self):
        fetched = {c.id: c for c in self.client.containers.list()}

        all_containers = {}
        all_containers.update(fetched)

        for cid in self.previous_containers:
            if cid not in all_containers:
                all_containers[cid] = self.containers[cid]

        self.previous_containers = fetched
        self.containers = all_containers

    def to_print_containers(self):
        return [f"{c.name}: {c.id}" for c in self.containers.values()]

    def collect_and_export_metrics(self, machine_name, executor):
        logger.info(f"Have {len(self.containers)} running containers, checking their metrics/stats...")

        get_metrics_tasks = []

        for c in self.containers.values():
            c_metrics_future = executor.submit(_container_metrics, c)
            get_metrics_tasks.append(c_metrics_future)

        for t in get_metrics_tasks:
            c_metrics = t.result()
            if c_metrics:
                metrics_exporter.on_new_container_metrics(machine_name, c_metrics)

        logger.info("\nMetrics checked.\n")

    def collect_and_export_logs(self, machine_name):
        logger.info(f"Have {len(self.containers)} running containers, checking their logs...")

        for c in self.containers.values():
            c_logs = _container_logs(c, self.containers_last_logs_check_timestamps)
            if c_logs:
                logs_exporter.export(machine_name, logs_exporter.ContainerLogs(c.name, c_logs))

        logger.info("\nLogs checked.\n")


def new_docker_client():
    return docker.DockerClient(base_url="unix://var/run/docker.sock")


def _container_metrics(container):
    try:
        c_metrics = container.stats(stream=False)

        memory_metrics = c_metrics["memory_stats"]
        prev_cpu_metrics = c_metrics["precpu_stats"]
        cpu_metrics = c_metrics["cpu_stats"]

        return _formatted_container_metrics(container=container,
                                            memory_metrics=memory_metrics,
                                            precpu_metrics=prev_cpu_metrics,
                                            cpu_metrics=cpu_metrics)
    except NotFound:
        logger.info(f"Container {container.name}:{container.id} not found, skipping!")
        return None
    except json.decoder.JSONDecodeError:
        logger.info(f"Container {container.name}:{container.id} returned invalid json, skipping!")
        return None
    except Exception:
        logger.exception(f"Failed to gather metrics of container {container.name} with state {container.status}")
        return None


def _container_available_cpus(container):
    host_config = container.attrs["HostConfig"]
    nano_cpus = int(host_config['NanoCpus'])
    # containers run without cpu limits have 0 nano_cpus
    if nano_cpus <= 0:
        return None

    return nano_cpus / CPU_NANOS


def _container_started_at(container):
    started_at = container.attrs['State']['StartedAt']
    # make date format compatible with python iso format impl
    started_at = re.sub("(\\.[0-9]+)", "", started_at).replace("Z", "")
    return int(datetime.fromisoformat(started_at).timestamp())


def _formatted_container_metrics(container, memory_metrics, precpu_metrics, cpu_metrics):
    try:
        system_cpus = cpu_metrics.get('online_cpus', 1)
        container_cpus = _container_available_cpus(container)

        cpu_usage = _container_cpu_metrics(precpu_metrics=precpu_metrics,
                                           cpu_metrics=cpu_metrics,
                                           system_cpus=system_cpus)

        return metrics_exporter.ContainerMetrics(container=container.name,
                                                 started_at=_container_started_at(container),
                                                 used_memory=memory_metrics["usage"],
                                                 max_memory=memory_metrics["limit"],
                                                 cpu_usage=cpu_usage,
                                                 cpus_available=container_cpus if container_cpus else system_cpus)
    except KeyError:
        # We get this, when container.stats() return empty/partial stats for killed/being killed container.
        # We don't care about that
        return None


def _container_cpu_metrics(precpu_metrics, cpu_metrics, system_cpus):
    prev_usage = precpu_metrics['cpu_usage']
    prev_container_usage = prev_usage['total_usage']
    prev_system_usage = precpu_metrics['system_cpu_usage']

    current_usage = cpu_metrics['cpu_usage']
    current_container_usage = current_usage['total_usage']
    current_system_usage = cpu_metrics['system_cpu_usage']

    container_delta = current_container_usage - prev_container_usage
    system_delta = current_system_usage - prev_system_usage

    if container_delta > 0 and system_delta > 0:
        container_system_delta_ratio = container_delta / system_delta
        cpu_usage = container_system_delta_ratio * system_cpus
        # Value is in the range of 0 - 1, so multiplying it by 100 we need only up to 2 digits precision like 12.34%
        return round(cpu_usage, 4)

    return 0


def _container_logs(container, containers_last_logs_check_timestamps):
    try:
        now = utils.current_timestamp()

        logs_since = containers_last_logs_check_timestamps.get(container.name, now - DEFAULT_LOGS_SINCE_NOW)

        c_logs = container.logs(since=logs_since, until=now, stream=False).decode('utf-8')

        containers_last_logs_check_timestamps[container.name] = now

        return c_logs
    except NotFound:
        logger.info(f"Container {container.name}:{container.id} not found, skipping!")
        return None
    except Exception:
        logger.exception(f"Failed to gather logs of container {container.name}")
        return None
