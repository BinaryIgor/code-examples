#!/bin/bash

cd tests

container_name="sql-db-performance-tests"
docker build . -t $container_name