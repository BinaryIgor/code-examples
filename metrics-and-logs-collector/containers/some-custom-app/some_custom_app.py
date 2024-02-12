from concurrent.futures import ProcessPoolExecutor
from os import environ
import logging
import signal
import sys
import time
import random

PROCESSES = int(environ.get("BUSY_CPUS", 2))

log = logging.getLogger("some_custom_app")
log.setLevel(level=logging.INFO)

console_formatter = logging.Formatter('%(asctime)s [%(levelname)s] %(name)s: %(message)s')
sh = logging.StreamHandler()
sh.setFormatter(console_formatter)
log.addHandler(sh)


def handle_exit_signals():
    # args argument required by the API
    def exit_gracefully(*args):
        print()
        log.info("Exit requested, stopping current script.")
        print()
        sys.exit(0)

    signal.signal(signal.SIGINT, exit_gracefully)
    signal.signal(signal.SIGTERM, exit_gracefully)


handle_exit_signals()

with ProcessPoolExecutor(max_workers=PROCESSES) as executor:
    while True:
        log.info("\nPerforming some pointless computations...")

        random_log = random.uniform(0, 1)
        if random_log >= 0.85:
            log.error(f"Random error: {random_log}")
        elif random_log >= 0.7:
            log.warning(f"Random warning: {random_log}")

        tasks = []

        def processing_task():
            for i in range(10_000_000):
                calc = i + time.time()
            return calc


        for i in range(PROCESSES):
            t = executor.submit(processing_task)
            tasks.append(t)

        for t in tasks:
            log.info(f"Computations result: {t.result()}")
