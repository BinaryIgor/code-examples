#!/bin/bash
set -e

export DATA_SOURCE_NAME='MySQL'
export DATA_SOURCE_URL="jdbc:mysql://localhost:3306/test"
export DATA_SOURCE_USERNAME=root
export DATA_SOURCE_PASSWORD=mysql

java -jar tests/target/sql-db-tests-jar-with-dependencies.jar