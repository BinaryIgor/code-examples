#!/bin/bash
set -euo pipefail

mysql_tests_host=$MYSQL_TESTS_HOST
postgresql_tests_host=$POSTGRESQL_TESTS_HOST
mariadb_tests_host=$MARIADB_TESTS_HOST

export APP="sql-db-performance-tests"
export APP_DIR="tests"
export SKIP_RUNNING="true"

echo "Deploying mysql tests..."

export DEPLOY_HOST=$mysql_tests_host
bash deploy.bash

echo
echo "mysql tests deployed to $mysql_tests_host, deploying run_test script..."

bash deploy_run_test_script.bash

echo
echo "postgresql tests & run_test script are deployed and ready to run!"

echo "Deploying postgresql tests..."

export DEPLOY_HOST=$postgresql_tests_host
bash deploy.bash

echo
echo "postgresql tests deployed to $postgresql_tests_host, deploying run_test script..."

bash deploy_run_test_script.bash

echo
echo "postgresql tests & run_test script are deployed and ready to run!"

echo "Deploying mariadb tests..."

export DEPLOY_HOST=$mariadb_tests_host
bash deploy.bash

echo
echo "mariadb tests deployed to $mariadb_tests_host, deploying run_test script..."

bash deploy_run_test_script.bash

echo
echo "mariadb tests & run_test script are deployed and ready to run!"

echo
echo "All tests & scripts are deployed and ready to run!"
