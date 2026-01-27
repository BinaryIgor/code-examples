#!/bin/bash
cd tests
container_name="json-dbs-performance-tests"
docker build . -t $container_name