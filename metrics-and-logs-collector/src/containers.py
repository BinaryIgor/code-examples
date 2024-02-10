import json
import re
import time
from datetime import datetime

import utils

import docker

import logs_exporter
import metrics_exporter

DEFAULT_LOGS_SINCE_NOW = 60

logger = utils.new_logger("containers")


class DockerContainers:
    """
    Wrapper class for containers states.
    We need previous_containers field to make sure that we collect metrics & logs from containers that died after previous collection,
    but before the next one
    """

    def __init__(self, docker_client):
        self.containers = []
        self.client = docker_client

    def get(self):
        fetched = {c.id: c for c in self.client.containers.list()}

        all_containers = {}
        all_containers.update(fetched)

        for cid in self.containers:
            if cid not in all_containers:
                all_containers[cid] = self.containers[cid]

        self.containers = fetched

        return all_containers

    def container(self, container_id):
        return self.containers[container_id]


def new_docker_client():
    return docker.DockerClient(base_url="unix://var/run/docker.sock")


def gather_and_export_metrics(machine_name, docker_containers):
    logger.info(f"Have {len(docker_containers.containers)} running containers, checking their metrics/stats...")

    for c in docker_containers.containers.values():
        print()
        logger.info(f"Checking {c.name}:{c.id} container metrics...")

        c_metrics = container_metrics(c)

        if c_metrics:
            metrics_exporter.on_new_container_metrics(machine_name, c_metrics)

        logger.info(f"{c.name}:{c.id} container metrics checked")

    print()
    logger.info("Metrics checked.")
    print()


# TODO: class method
def container_metrics(container):
    try:
        c_metrics = container.stats(stream=False)

        memory_metrics = c_metrics["memory_stats"]
        prev_cpu_metrics = c_metrics["precpu_stats"]
        cpu_metrics = c_metrics["cpu_stats"]

        return formatted_container_metrics(container=container,
                                           memory_metrics=memory_metrics,
                                           precpu_metrics=prev_cpu_metrics,
                                           cpu_metrics=cpu_metrics)
    except json.decoder.JSONDecodeError:
        logger.info(f"Container {container.name}:{container.id} returned invalid json, skipping!")
        print()
        return None
    except Exception:
        logger.exception(f"Failed to gather metrics of container {container.name} with state {container.status}")
        return None


def container_available_cpus(container):
    nanos = 1_000_000_000.0
    host_config = container.attrs["HostConfig"]
    return int(host_config['NanoCpus']) / nanos


def container_started_at(container):
    started_at = container.attrs['State']['StartedAt']
    # make date format compatible with python iso format impl
    started_at = re.sub("(\\.[0-9]+)", "", started_at).replace("Z", "")
    return int(datetime.fromisoformat(started_at).timestamp())


def formatted_container_metrics(container, memory_metrics, precpu_metrics, cpu_metrics):
    try:
        cpu_usage = container_cpu_metrics(precpu_metrics, cpu_metrics, container_available_cpus(container))
        return metrics_exporter.ContainerMetrics(container=container.name,
                                                 started_at=container_started_at(container),
                                                 used_memory=memory_metrics["usage"],
                                                 max_memory=memory_metrics["limit"],
                                                 cpu_usage=cpu_usage)
    except KeyError:
        # We get this, when container.stats() return empty/partial stats for killed/being killed container.
        # We don't care about that
        return None


def container_cpu_metrics(precpu_metrics, cpu_metrics, container_cpus):
    prev_usage = precpu_metrics['cpu_usage']
    prev_container_usage = prev_usage['total_usage']
    prev_system_usage = precpu_metrics['system_cpu_usage']

    current_usage = cpu_metrics['cpu_usage']
    current_container_usage = current_usage['total_usage']
    current_system_usage = cpu_metrics['system_cpu_usage']

    system_cpus = cpu_metrics.get('online_cpus', 1)

    container_delta = current_container_usage - prev_container_usage
    system_delta = current_system_usage - prev_system_usage

    if container_delta > 0 and system_delta > 0:
        container_system_delta_ratio = container_delta / system_delta
        cpu_usage = (container_system_delta_ratio * system_cpus) / container_cpus
        # Value is in the range of 0 - 1, so multiplying it by 100 we need only up to 2 digits precision like 12.34%
        return round(cpu_usage, 4)

    return 0


def gather_and_export_logs(machine_name, docker_containers, containers_last_logs_check_timestamps):
    logger.info(f"Have {len(docker_containers.containers)} running containers, checking their logs...")

    for c in docker_containers.containers.values():
        logger.info(f"Checking {c.name}:{c.id} container logs...")

        c_logs = container_logs(c, containers_last_logs_check_timestamps)
        if c_logs:
            logs_exporter.export(machine_name, logs_exporter.ContainerLogs(c.name, c_logs))

        logger.info(f"{c.name}:{c.id} container logs checked")


def container_logs(container, containers_last_logs_check_timestamps):
    try:
        now = _current_timestamp()

        logs_since = containers_last_logs_check_timestamps.get(container.name, now - DEFAULT_LOGS_SINCE_NOW)

        c_logs = container.logs(since=logs_since, until=now, stream=False).decode('utf-8').strip()

        containers_last_logs_check_timestamps[container.name] = now

        return c_logs
    except Exception:
        logger.exception(f"Failed to gather logs of container {container.name}")
        return None


def _current_timestamp():
    return int(time.time())
