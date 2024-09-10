#!/bin/bash
set -eu

mvn clean package -DskipTests

java -jar target/vector-embeddings-with-postgres.jar