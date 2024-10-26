#!/bin/bash
set -e

export DATA_SOURCE_NAME='Postgres'
export DATA_SOURCE_URL="jdbc:postgresql://localhost:5432/test"
export DATA_SOURCE_USERNAME=postgres
export DATA_SOURCE_PASSWORD=postgres

java -jar tests/target/sql-db-tests-jar-with-dependencies.jar