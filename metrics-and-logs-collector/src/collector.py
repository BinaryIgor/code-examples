import json
import random
import signal
import sys
import time
from concurrent.futures import ThreadPoolExecutor
from os import environ

import containers
import metrics_exporter
import utils

logger = utils.new_logger("collector")

MACHINE_NAME = environ.get("MACHINE_NAME", "local-machine")

METRICS_COLLECTION_INTERVAL = int(environ.get("METRICS_COLLECTION_INTERVAL", 20))
LOGS_COLLECTION_INTERVAL = int(environ.get("LOGS_COLLECTION_INTERVAL", 5))

LAST_METRICS_READ_AT_FILE_PATH = environ.get("LAST_METRICS_READ_AT_FILE",
                                             "/tmp/last-metrics-read-at.txt")
LAST_LOGS_READ_AT_FILE_PATH = environ.get("LAST_LOGS_READ_AT_FILE",
                                          "/tmp/last-logs-read-at.txt")

MAX_COLLECTOR_THREADS = environ.get("max_collector_threads", 3)

MAX_LOGS_NOT_SEND_AGO = 10 * 60

METRICS_EXPORTER_PORT = int(environ.get("METRICS_EXPORTER_PORT", 8080))


class GracefulShutdown:
    stop = False

    def __init__(self):
        signal.signal(signal.SIGINT, self.exit_gracefully)
        signal.signal(signal.SIGTERM, self.exit_gracefully)

    # Args are needed due to signal handler specification
    def exit_gracefully(self, *args):
        self.stop = True


shutdown = GracefulShutdown()


def data_object_formatted(data_object):
    return json.dumps(data_object, indent=2)


def random_retry_interval():
    return round(random.uniform(1, 5), 3)


def log_exception(message):
    logger.exception(f"{message}")
    print()


def current_timestamp():
    return int(time.time())


def current_timestamp_millis():
    return int(time.time() * 1000)


def connected_docker_client_retrying():
    logger.info(f"Starting monitoring of {MACHINE_NAME}...")

    while True:
        try:
            logger.info("Trying to get client...")
            client = containers.new_docker_client()
            ver = data_object_formatted(client.version())
            logger.info(f"Client connected, docker version: {ver}")
            return client
        except Exception:
            if shutdown.stop:
                logger.info("Shutdown requested, exiting")
                sys.exit(0)

            retry_interval = random_retry_interval()
            log_exception(f"Problem while connecting to docker client, retrying in {retry_interval}s...")
            time.sleep(retry_interval)


docker_containers = containers.DockerContainers(connected_docker_client_retrying())


def keep_collecting_and_exporting():
    try:
        do_keep_collecting_and_exporting()
    except Exception:
        log_exception("Problem while collecting, retrying...")
        keep_collecting_and_exporting()


# TODO: thread pool for better collection
def do_keep_collecting_and_exporting():
    collection_interval = min(METRICS_COLLECTION_INTERVAL, LOGS_COLLECTION_INTERVAL)
    last_metrics_collection_timestamp = 0
    last_logs_collection_timestamp = 0

    containers_last_logs_check_timestamps = {}

    with ThreadPoolExecutor(max_workers=MAX_COLLECTOR_THREADS) as executor:
        while True:
            if shutdown.stop:
                logger.info("Shutdown requested, exiting gracefully")
                break

            timestamp = current_timestamp()

            logger.info("Checking containers...")

            running_containers = docker_containers.get()

            logger.info(f"Running containers: {running_containers}")

            should_gather_metrics = timestamp - last_metrics_collection_timestamp >= METRICS_COLLECTION_INTERVAL
            should_gather_logs = timestamp - last_logs_collection_timestamp >= LOGS_COLLECTION_INTERVAL

            if should_gather_metrics:
                containers.gather_and_export_metrics(MACHINE_NAME, docker_containers)
                last_metrics_collection_timestamp = current_timestamp()
            if should_gather_logs:
                containers.gather_and_export_logs(MACHINE_NAME, docker_containers,
                                                  containers_last_logs_check_timestamps)
                last_logs_collection_timestamp = current_timestamp()

            print("...")

            metrics_exporter.on_next_collection(MACHINE_NAME)

            if shutdown.stop:
                logger.info("Shutdown requested, existing gracefully")

            logger.info(f"Sleeping for {collection_interval}s")
            print()

            time.sleep(collection_interval)


metrics_exporter.export(METRICS_EXPORTER_PORT)
keep_collecting_and_exporting()
