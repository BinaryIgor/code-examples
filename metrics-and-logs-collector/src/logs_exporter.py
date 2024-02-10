import json
import logging
import pathlib
from logging.handlers import RotatingFileHandler
from os import environ, path

import metrics_exporter
import utils


class ContainerLogs:

    def __init__(self, container, logs):
        self.container = container
        self.logs = logs


LOGS_DIR = environ.get("LOGS_DIR", path.join("/tmp", "logs"))
pathlib.Path(LOGS_DIR).mkdir(exist_ok=True)

LOGS_CONTAINER_MAX_FILES = int(environ.get("LOGS_CONTAINER_MAX_FILES", "10"))
LOGS_CONTAINER_MAX_FILE_SIZE = int(environ.get("LOGS_CONTAINER_MAX_FILE_SIZE", 10 * 1024 * 1024))

containers_loggers = {}
containers_log_levels_mapping = {}

LOG_LEVELS_MAPPING_PATH = environ.get("LOG_LEVELS_MAPPING_PATH", path.join("config", "log_levels_mapping.json"))

with open(LOG_LEVELS_MAPPING_PATH) as f:
    log_levels_mapping = json.load(f)

DEFAULT_MAPPING = "default_mapping"
WARNING_KEYWORDS = "warning_keywords"
ERROR_KEYWORDS = "error_keywords"
MAPPINGS = "mappings"
CONTAINER_KEYWORDS = "container_keywords"
MESSAGES_TO_IGNORE_ERRORS = "messages_to_ignore_errors"

INFO = "info"
WARNING = "warning"
ERROR = "error"

logger = utils.new_logger("logs_exporter")


def print_config():
    logger.info(f"LOGS_DIR: {LOGS_DIR}")
    logger.info(f"LOGS_CONTAINER_MAX_FILES: {LOGS_CONTAINER_MAX_FILES}")
    logger.info(f"LOGS_CONTAINER_MAX_FILE_SIZE: {LOGS_CONTAINER_MAX_FILE_SIZE}")
    logger.info(f"LOG_LEVELS_MAPPING_PATH: {LOG_LEVELS_MAPPING_PATH}")


def export(machine, container_logs):
    _log_to_file(container_logs)
    _export_metrics(machine, container_logs)


def _log_to_file(container_logs):
    container = container_logs.container

    container_logger = containers_loggers.get(container)

    if not container_logger:
        container_logs_dir = path.join(LOGS_DIR, container)

        pathlib.Path(container_logs_dir).mkdir(exist_ok=True)
        log_file = path.join(container_logs_dir, f"{container_logs.container}.log")

        handler = RotatingFileHandler(log_file,
                                      mode='a',
                                      maxBytes=LOGS_CONTAINER_MAX_FILE_SIZE,
                                      backupCount=LOGS_CONTAINER_MAX_FILES)

        container_logger = logging.getLogger(container)
        container_logger.setLevel(level=logging.INFO)

        container_logger.addHandler(handler)

        containers_loggers[container] = container_logger

        logs_level_mapping = _log_levels_mapping(container)
        containers_log_levels_mapping[container] = logs_level_mapping

    container_logger.info(container_logs.logs)


def _export_metrics(machine, container_logs):
    levels_mapping = containers_log_levels_mapping[container_logs.container]
    logs_level = _logs_level(levels_mapping, container_logs.logs)
    metrics_exporter.on_new_container_logs(machine, container_logs.container, logs_level)


def _log_levels_mapping(container):
    levels_mapping = log_levels_mapping[DEFAULT_MAPPING]

    for lm in log_levels_mapping.get(MAPPINGS, []):
        container_mapping = False

        for c_keyword in lm[CONTAINER_KEYWORDS]:
            if c_keyword in container:
                container_mapping = True
                break

        if container_mapping:
            levels_mapping = lm
            break

    return levels_mapping


def _logs_level(levels_mapping, logs):
    def should_ignore_error(to_ignore_errors):
        for m in to_ignore_errors:
            if m in logs:
                return True

        return False

    messages_to_ignore_errors = levels_mapping.get(MESSAGES_TO_IGNORE_ERRORS, [])
    for ek in levels_mapping[ERROR_KEYWORDS]:
        if ek in logs:
            return INFO if should_ignore_error(messages_to_ignore_errors) else ERROR

    for wk in levels_mapping[WARNING_KEYWORDS]:
        if wk in logs:
            return WARNING

    return INFO
