#!/usr/bin/env python3
import subprocess as sp
import sys
from os import environ

TEST_CONTAINER_NAME = "json-dbs-performance-tests"

try:
    options_to_test_cases = {
        1: 'INSERT_ACCOUNTS',
        2: 'INSERT_PRODUCTS',
        3: 'BATCH_INSERT_ACCOUNTS',
        4: 'BATCH_INSERT_PRODUCTS',
        5: 'UPDATE_ACCOUNTS',
        6: 'UPDATE_PRODUCTS',
        7: 'FIND_ACCOUNTS_BY_ID',
        8: 'FIND_PRODUCTS_BY_ID',
        9: 'FIND_SORTED_BY_CREATED_AT_ACCOUNTS_PAGES',
        10: 'FIND_ACCOUNTS_BY_OWNERS',
        11: 'FIND_PRODUCTS_BY_TAGS',
        12: 'FIND_ACCOUNTS_STATS_BY_IDS',
        13: 'FIND_PRODUCTS_STATS_BY_IDS',
        14: 'INSERT_UPDATE_DELETE_FIND_ACCOUNTS',
        15: 'DELETE_ACCOUNTS',
        16: 'DELETE_PRODUCTS',
        17: 'BATCH_DELETE_ACCOUNTS'
    }

    options = '\n'.join([f'{k} - {v}' for k, v in options_to_test_cases.items()])

    test_case_input = input(f"""
Choose test case. Available options:
{options}
""")

    if not test_case_input:
        print("Test case must be chosen but was not!")
        sys.exit(-1)

    test_case = options_to_test_cases[int(test_case_input)]
    print(f"Chosen test case: {test_case}")

    db_type = int(input(f"""
Choose db. Available options:
1 - MONGODB
2 - POSTGRESQL
""").strip() or 1)
    
    print()

    # 8 cores available - usually a few connections per core is where the optimal amount lives;
    # here, we are stress/load testing - it's not about the best absolute amount; better to use too many than too few connections
    db_host = environ.get('DB_HOST', 'localhost')
    db_user = "json"
    db_password = "json"
    if db_type == 1:
        db_type_name = "mongodb"
        db_connection_pool_size = 256
        db_url = f"mongodb://{db_user}:{db_password}@{db_host}:27017/json?authSource=admin&minPoolSize={db_connection_pool_size}&maxPoolSize={db_connection_pool_size}"
        print("Running with MongoDB")
    elif db_type == 2:
        db_type_name = "postgresql"
        db_url = f"jdbc:postgresql://{db_host}:5432/json"
        db_connection_pool_size = 64
        print("Running with PostgreSQL")
    else:
        print("Unsupported db type chosen!")
        sys.exit(-1)

    print()

    QUERIES_TO_EXECUTE = environ.get('QUERIES_TO_EXECUTE')
    QUERIES_RATE = environ.get('QUERIES_RATE')

    test_env = "-e DB_URL -e DB_USER -e DB_PASSWORD -e DB_CONNECTION_POOL_SIZE -e TEST_CASE"

    if QUERIES_TO_EXECUTE:
        test_env += " -e QUERIES_TO_EXECUTE"
    if QUERIES_RATE:
        test_env += " -e QUERIES_RATE"

    run_test_cmd = f"""
    docker rm {TEST_CONTAINER_NAME} || true
    echo
    docker run --network host \
    {test_env} \
    --name {TEST_CONTAINER_NAME} {TEST_CONTAINER_NAME}
    """.strip()

    script_result = sp.run(f"""
    #!/bin/bash
    set -e
    
    export DB_URL="{db_url}"
    export DB_USER="{db_user}"
    export DB_PASSWORD="{db_password}"
    export DB_CONNECTION_POOL_SIZE="{db_connection_pool_size}"
    export TEST_CASE="{test_case}"

    {run_test_cmd}
""", shell=True)

    print()
    print("Tests have finished running, exporting results to a file...")

    results_file = f"{test_case.lower()}_{db_type_name}"
    if QUERIES_RATE:
        results_file += f"_{QUERIES_RATE}_qps.txt"
    else:
        results_file += f".txt"

    sp.run(f"docker logs {TEST_CONTAINER_NAME} > {results_file} 2>&1", shell=True)

    print()
    print(f"Results exported to the {results_file} file")
except KeyboardInterrupt:
    print("Process interrupted by user, exiting")