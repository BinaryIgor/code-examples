import math
import statistics
import time
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor
import requests as requests_lib
import random
import uuid
import traceback

target_host = "http://164.92.137.232:8080"

existing_account1 = '7037b56f-15ae-46f3-a2c6-d098411619a1'
existing_account2 = '44309de7-62f2-4886-8b17-03411cccff9d'
existing_account1_name = f'name-{existing_account1}'
existing_account2_name = f'name-{existing_account2}'

def account_endpoint(account_id):
    return f"{target_host}/accounts/{account_id}"  

def accounts_by_name_endpoint(name):  
    return f"{target_host}/accounts?name={name}"

fast_endpoints = [account_endpoint(existing_account1),
                  account_endpoint(existing_account2),
                  account_endpoint(str(uuid.uuid4())),
                  account_endpoint(str(uuid.uuid4()))]

slow_endpoints = [accounts_by_name_endpoint(existing_account1_name),
                  accounts_by_name_endpoint(existing_account2_name),
                  accounts_by_name_endpoint(str(uuid.uuid4())),
                  accounts_by_name_endpoint(str(uuid.uuid4()))]

args = {
    "requests": 5000,
    "rate_per_second": 500,
    "endpoints": fast_endpoints
}

endpoints = args["endpoints"]

print("About to execute http load test using endpoints:")
print(endpoints)

def task():
    # Make it more uniform, so that not all requests start at once
    # delay = random.uniform(0, 0.5)
    # time.sleep(delay)

    start = time.time()

    endpoint = random.choice(endpoints)
    try:
        response = requests_lib.get(endpoint, timeout=(5, 10))
    except:
        print("Timeout or other problems!")
        traceback.print_exc()
    # print(response.json())

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
parallelism = min(2 * rate_per_second, 100)

start = time.time()

try:
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

    min = formatted_seconds(min(sorted_results))
    max = formatted_seconds(max(sorted_results))
    mean = formatted_seconds(statistics.mean(sorted_results))
    median = formatted_seconds(statistics.median(sorted_results))
    std = formatted_seconds(statistics.stdev(sorted_results))
    percentile_75 = formatted_seconds(percentile(sorted_results, 75))
    percentile_90 = formatted_seconds(percentile(sorted_results, 90))
    percentile_95 = formatted_seconds(percentile(sorted_results, 95))
    percentile_99 = formatted_seconds(percentile(sorted_results, 99))
    percentile_100 = formatted_seconds(percentile(sorted_results, 100))

    print("Stats in seconds...")
    print(f"Min: {min}")
    print(f"Max: {max}")
    print(f"Mean: {mean}")
    print(f"Median: {median}")
    print(f"Std: {std}")
    print(f"Percentile 75: {percentile_75}")
    print(f"Percentile 90: {percentile_90}")
    print(f"Percentile 95: {percentile_95}")
    print(f"Percentile 99: {percentile_99}")
    print(f"Percentile 100: {percentile_100}")
except KeyboardInterrupt:
    print("Test interrupted by the user!")
