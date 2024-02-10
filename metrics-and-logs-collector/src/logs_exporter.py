import logging
import pathlib
from logging.handlers import RotatingFileHandler
from os import environ, path


class ContainerLogs:

    def __init__(self, container, logs):
        self.container = container
        self.logs = logs


LOGS_DIR = environ.get("LOGS_DIR", path.join("/tmp", "logs"))
pathlib.Path(LOGS_DIR).mkdir(exist_ok=True)

containers_loggers = {}

# LOGS_CONFIG_PATH = environ.get("LOGS_CONFIG_PATH", "logs_config.json")
#
# with open(LOGS_CONFIG_PATH) as f:
#     logs_config = json.load(f)

INFO = "info"
WARNING = "warning"
ERROR = "error"


def export(machine, container_logs):
    _log_to_file(container_logs)


def _log_to_file(container_logs):
    logger = containers_loggers.get(container_logs.container)

    if not logger:
        # TODO: more configurable
        container_logs_dir = path.join(LOGS_DIR, container_logs.container)

        pathlib.Path(container_logs_dir).mkdir(exist_ok=True)
        log_file = path.join(container_logs_dir, f"{container_logs.container}.log")

        handler = RotatingFileHandler(log_file,
                                      mode='a',
                                      maxBytes=10 * 1024 * 1024,
                                      backupCount=10)

        logger = logging.getLogger(container_logs.container)
        logger.setLevel(level=logging.INFO)

        logger.addHandler(handler)

        containers_loggers[container_logs.container] = logger

    logger.info(container_logs.logs)
