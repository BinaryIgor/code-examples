import concurrent.futures
import logging
import signal
import sys
import time

PROCESSES = 3

log = logging.getLogger("some_app")
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

with concurrent.futures.ProcessPoolExecutor(max_workers=PROCESSES) as executor:
    while True:
        log.info("Spamming with logs...")

        tasks = []

        def processing_task():
            for i in range(10_000_000):
                calc = i + time.time()


        for i in range(PROCESSES):
            t = executor.submit(processing_task)
            tasks.append(t)

        concurrent.futures.wait(tasks)
