#!/bin/bash

container_name="sqlite-limits-tests"
volume_dir="${SQLITE_TESTS_VOLUME_DIR:-${HOME}/${container_name}_volume}"

docker stop $container_name
docker rm $container_name

export JOURNAL_MODE='WAL';
export DB_DIRECTORY="/db";
export TEST_CASE_CPUS=${TEST_CASE_CPUS:-2}

docker run -v "${volume_dir}:/db" \
  -e JOURNAL_MODE -e DB_DIRECTORY -e BEFORE_TESTS_QUERY \
  -e TEST_CASE \
  -e QUERIES_TO_EXECUTE -e QUERIES_BATCH_SIZE -e QUERIES_RATE \
  --memory "1G" --cpus "$TEST_CASE_CPUS" \
  --name $container_name $container_name