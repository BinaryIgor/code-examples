#!/bin/bash
set -euo pipefail

mysql_host=$MYSQL_HOST
postgresql_host=$POSTGRESQL_HOST
mariadb_host=$MARIADB_HOST

echo "Deploying mysql..."

export APP="mysql-performance"
export APP_DIR="mysql"
export DEPLOY_HOST=$mysql_host
bash deploy.bash

echo
echo "mysql deployed to $mysql_host, deploying postgresql..."

export APP="postgresql-performance"
export APP_DIR="postgresql"
export DEPLOY_HOST=$postgresql_host
bash deploy.bash

echo
echo "postgresql deployed to $postgresql_host, deploying mariadb..."

export APP="mariadb-performance"
export APP_DIR="mariadb"
export DEPLOY_HOST=$mariadb_host
bash deploy.bash

echo
echo "mariadb deployed to $mariadb_host, all dbs are deployed!"
