import signal
import sys
import time
from concurrent.futures import ThreadPoolExecutor
from os import environ

import logs_exporter
import metrics_exporter
import utils
from containers import Containers, new_docker_client

logger = utils.new_logger("collector")

MACHINE_NAME = environ.get("MACHINE_NAME", "local-machine")

METRICS_COLLECTION_INTERVAL = int(environ.get("METRICS_COLLECTION_INTERVAL", 20))
LOGS_COLLECTION_INTERVAL = int(environ.get("LOGS_COLLECTION_INTERVAL", 5))

LAST_METRICS_COLLECTED_AT_FILE = environ.get("LAST_METRICS_COLLECTED_AT_FILE",
                                             "/tmp/last-metrics-collected-at.txt")
LAST_LOGS_COLLECTED_AT_FILE = environ.get("LAST_LOGS_COLLECTED_AT_FILE",
                                          "/tmp/last-logs-collected-at.txt")

MAX_COLLECTOR_THREADS = environ.get("MAX_COLLECTOR_THREADS", 5)

METRICS_EXPORTER_PORT = int(environ.get("METRICS_EXPORTER_PORT", 10101))


class GracefulShutdown:
    stop = False

    def __init__(self):
        signal.signal(signal.SIGINT, self.exit_gracefully)
        signal.signal(signal.SIGTERM, self.exit_gracefully)

    # Args are needed due to signal handler specification
    def exit_gracefully(self, *args):
        self.stop = True


shutdown = GracefulShutdown()

logger.info(f"Starting collector for {MACHINE_NAME} machine!")
logger.info(f"METRICS_COLLECTION_INTERVAL: {METRICS_COLLECTION_INTERVAL}")
logger.info(f"LOGS_COLLECTION_INTERVAL: {LOGS_COLLECTION_INTERVAL}")
logger.info(f"MAX_COLLECTOR_THREADS: {MAX_COLLECTOR_THREADS}")
logger.info(f"LAST_METRICS_COLLECTED_AT_FILE: {LAST_METRICS_COLLECTED_AT_FILE}")
logger.info(f"LAST_LOGS_COLLECTED_AT_FILE: {LAST_LOGS_COLLECTED_AT_FILE}")

logs_exporter.print_config()
print()

if LOGS_COLLECTION_INTERVAL > METRICS_COLLECTION_INTERVAL:
    raise Exception("LOGS_COLLECTION_INTERVAL needs to be <= METRICS_COLLECTION_INTERVAL!")


def connected_docker_client_retrying():
    while True:
        try:
            logger.info("Trying to get client...")
            client = new_docker_client()
            ver = utils.pretty_data_object(client.version())
            logger.info(f"Client connected, docker version: {ver}\n")
            return client
        except Exception:
            if shutdown.stop:
                logger.info("Shutdown requested, exiting")
                sys.exit(0)

            retry_interval = utils.random_retry_interval()
            logger.exception(f"Problem while connecting to docker client, retrying in {retry_interval}s...")
            time.sleep(retry_interval)


containers = Containers(connected_docker_client_retrying())


def keep_collecting_and_exporting():
    try:
        do_keep_collecting_and_exporting()
    except Exception:
        logger.exception("Problem while collecting, retrying...")
        keep_collecting_and_exporting()


def do_keep_collecting_and_exporting():
    # LOGS_COLLECTION_INTERVAL is always <= METRICS_COLLECTION_INTERVAL
    collection_interval = LOGS_COLLECTION_INTERVAL
    last_metrics_collection_timestamp = 0

    with ThreadPoolExecutor(max_workers=MAX_COLLECTOR_THREADS) as executor:
        while True:
            if shutdown.stop:
                logger.info("Shutdown requested, exiting gracefully")
                break

            timestamp = utils.current_timestamp()

            logger.info("Checking containers...")

            containers.fetch()

            logger.info(f"To check containers: {containers.to_print_containers()}")

            should_collect_metrics = (timestamp - last_metrics_collection_timestamp) >= METRICS_COLLECTION_INTERVAL
            if should_collect_metrics:
                containers.collect_and_export_metrics(MACHINE_NAME, executor)
                last_metrics_collection_timestamp = utils.current_timestamp()
                update_last_data_collected_at_file(LAST_METRICS_COLLECTED_AT_FILE, last_metrics_collection_timestamp)

            # Logs collection is fast, we don't need to use a thread pool
            containers.collect_and_export_logs(MACHINE_NAME)
            last_logs_collection_timestamp = utils.current_timestamp()
            update_last_data_collected_at_file(LAST_LOGS_COLLECTED_AT_FILE, last_logs_collection_timestamp)

            metrics_exporter.on_next_collection(MACHINE_NAME)

            if shutdown.stop:
                logger.info("Shutdown requested, exiting gracefully")

            logger.info(f"\nSleeping for {collection_interval}s...\n")

            time.sleep(collection_interval)


def update_last_data_collected_at_file(file, collected_at):
    try:
        logger.info(f"Updating last-data-collected-at file: {file}")
        with open(file, "w") as f:
            f.write(str(collected_at))
    except Exception:
        logger.exception("Problem while updating last data collected at file...")


metrics_exporter.export(METRICS_EXPORTER_PORT)
logger.info(f"Metrics are exported on port {METRICS_EXPORTER_PORT}\n")

keep_collecting_and_exporting()
