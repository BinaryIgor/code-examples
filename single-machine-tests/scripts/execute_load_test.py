import math
import statistics
import time
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor
import requests as requests_lib
import random

args = {
    "requests": 5000,
    "rate_per_second": 500,
    "endpoints": ["http://164.92.137.232:8080/accounts"]
}

endpoints = args["endpoints"]

print("About to execute http load test using endpoints:")
print(endpoints)

def task():
    # Make it more uniform, so that not all requests start at once
    delay = random.uniform(0, 0.5)
    time.sleep(delay)

    start = time.time()

    endpoint = random.choice(endpoints)
    response = requests_lib.get(endpoint)

    return time.time() - start


def percentile(data, percentile):
    if percentile >= 100:
        return data[-1]

    if percentile <= 1:
        return data[0]

    n = len(data)
    if n == 0:
        raise Exception("no percentile for empty data")

    index = n * percentile / 100

    if index.is_integer():
        return data[round(index)]

    lower_idx = math.floor(index)
    upper_idx = math.ceil(index)

    if lower_idx < 0:
        return data[upper_idx]

    if upper_idx >= n:
        return data[lower_idx]

    return (data[lower_idx] + data[upper_idx]) / 2


rate_per_second = args["rate_per_second"]

requests = args["requests"]
parallelism = min(2 * rate_per_second, 250)

start = time.time()

with ThreadPoolExecutor(max_workers=parallelism) as executor:
    futures = []
    for i in range(requests):
        future = executor.submit(task)
        futures.append(future)

        issued_requests = i + 1
        if issued_requests % rate_per_second == 0 and issued_requests < requests:
            print(f"{datetime.now()}, {issued_requests} requests were issued, waiting 1s for the next packet...")
            time.sleep(1)

    results = [f.result() for f in futures]

duration = round(time.time() - start, 2)

print(f"{requests} requests with {rate_per_second} per second rate took {duration} seconds")

sorted_results = sorted(results)

def formatted_seconds(num):
    return round(num, 3)

mean = formatted_seconds(statistics.mean(sorted_results))
median = formatted_seconds(statistics.median(sorted_results))
std = formatted_seconds(statistics.stdev(sorted_results))
percentile_75 = formatted_seconds(percentile(sorted_results, 75))
percentile_90 = formatted_seconds(percentile(sorted_results, 90))
percentile_95 = formatted_seconds(percentile(sorted_results, 95))
percentile_99 = formatted_seconds(percentile(sorted_results, 99))
percentile_100 = formatted_seconds(percentile(sorted_results, 100))

print("Stats in seconds...")
print(f"Mean: {mean}")
print(f"Median: {median}")
print(f"Std: {std}")
print(f"Percentile 75: {percentile_75}")
print(f"Percentile 90: {percentile_90}")
print(f"Percentile 95: {percentile_95}")
print(f"Percentile 99: {percentile_99}")
print(f"Percentile 100: {percentile_100}")
