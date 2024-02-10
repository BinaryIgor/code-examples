import logging


def new_logger(name):
    formatter = logging.Formatter("%(asctime)s.%(msecs)03d [%(levelname)s] %(name)s: %(message)s", "%Y-%m-%d %H:%M:%S")
    sh = logging.StreamHandler()
    sh.setFormatter(formatter)

    logger = logging.getLogger(name)
    logger.setLevel(level=logging.INFO)
    logger.addHandler(sh)

    return logger
