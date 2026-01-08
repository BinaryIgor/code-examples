#!/bin/python3
import subprocess as sp
import sys
from os import environ

try:
    options_to_test_cases = {
        1: 'INSERT_USERS',
        2: 'INSERT_ITEMS_IN_BATCHES',
        3: 'INSERT_ORDERS_IN_BATCHES',
        4: 'INSERT_ORDER_ITEMS_IN_BATCHES',
        5: 'SELECT_USERS_BY_ID',
        6: 'SELECT_USERS_BY_EMAIL',
        7: 'SELECT_SORTED_BY_ID_USER_PAGES',
        8: 'SELECT_ORDERS_JOINED_WITH_USERS',
        9: 'SELECT_ORDERS_JOINED_WITH_ITEMS',
        10: 'SELECT_USERS_WITH_ORDERS_STATS_BY_ID',
        11: 'UPDATE_USER_EMAILS_BY_ID',
        12: 'UPDATE_USER_UPDATED_ATS_BY_ID',
        13: 'UPDATE_USER_MULTIPLE_COLUMNS_BY_ID',
        14: 'DELETE_ORDERS_BY_ID',
        15: 'DELETE_ORDERS_IN_BATCHES_BY_ID',
        16: 'INSERT_USERS_AND_ORDERS_WITH_ITEMS_IN_TRANSACTIONS',
        17: 'INSERT_UPDATE_DELETE_AND_SELECT_USERS_BY_ID'
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
1 - MySQL
2 - PostgreSQL
3 - MariaDB
""").strip() or 1)

    print()

    # 8 cores available - usually a few connections per core is where the optimal amount lives;
    # here, we are stress/load testing - it's not about the best absolute amount; better to use too many than too few connections
    # Empirically, MySQL and MariaDB benefit from more connections
    if db_type == 1:
        data_source_url = "jdbc:mysql://localhost:3306/performance"
        data_source_username = "root"
        data_source_password = "performance"
        data_source_connection_pool_size = environ.get("DATA_SOURCE_CONNECTION_POOL_SIZE", 8 * 16)
        print("Running with MySQL")
    elif db_type == 2:
        data_source_url = "jdbc:postgresql://localhost:5432/performance"
        data_source_username = "postgres"
        data_source_password = "performance"
        data_source_connection_pool_size = environ.get("DATA_SOURCE_CONNECTION_POOL_SIZE", 8 * 8)
        print("Running with PostgreSQL")
    else:
        data_source_url = "jdbc:mariadb://localhost:3306/performance"
        data_source_username = "root"
        data_source_password = "performance"
        data_source_connection_pool_size = environ.get("DATA_SOURCE_CONNECTION_POOL_SIZE", 8 * 16)
        print("Running with MariaDB")

    print()

    QUERIES_TO_EXECUTE = environ.get("QUERIES_TO_EXECUTE")
    QUERIES_RATE = environ.get("QUERIES_RATE")

    docker_env = "-e DATA_SOURCE_URL -e DATA_SOURCE_USERNAME -e DATA_SOURCE_PASSWORD -e DATA_SOURCE_CONNECTION_POOL_SIZE -e TEST_CASE"
    if QUERIES_TO_EXECUTE:
        docker_env += " -e QUERIES_TO_EXECUTE"
    if QUERIES_RATE:
        docker_env += " -e QUERIES_RATE"
    run_test_in_docker = f"""
    docker rm sql-db-performance-tests || true
    echo
    docker run --network host \
      {docker_env} \
      --name sql-db-performance-tests sql-db-performance-tests
    """.strip()

    run_test_directly = "java -jar tests/target/sql-db-performance-tests-jar-with-dependencies.jar"

    script_result = sp.run(f"""
    #!/bin/bash
    set -e
    
    export DATA_SOURCE_URL="{data_source_url}"
    export DATA_SOURCE_USERNAME="{data_source_username}"
    export DATA_SOURCE_PASSWORD="{data_source_password}"
    export DATA_SOURCE_CONNECTION_POOL_SIZE={data_source_connection_pool_size}
    export TEST_CASE="{test_case}"
    
    {run_test_in_docker}
    """, shell=True)
except KeyboardInterrupt:
    print("Process interrupted by user, exiting")