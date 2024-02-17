import json
import logging
import random
import time


def new_logger(name):
    formatter = logging.Formatter("%(asctime)s.%(msecs)03d [%(levelname)s] %(name)s: %(message)s", "%Y-%m-%d %H:%M:%S")
    sh = logging.StreamHandler()
    sh.setFormatter(formatter)

    logger = logging.getLogger(name)
    logger.setLevel(level=logging.INFO)
    logger.addHandler(sh)

    return logger


def pretty_data_object(data_object):
    return json.dumps(data_object, indent=2)


def random_retry_interval():
    return round(random.uniform(1, 5), 3)


def current_timestamp():
    return int(time.time())


def current_timestamp_millis():
    return int(time.time() * 1000)
