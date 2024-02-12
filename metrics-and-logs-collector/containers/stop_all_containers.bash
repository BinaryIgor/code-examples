#!/bin/bash

containers=("postgres-db" "prometheus" "some-custom-app" "logs-browser")

echo "About to stop containers: ${containers[@]}"
echo

for c in ${containers[@]}; do
  docker stop $c
done

echo
echo "All containers (except metrics-and-logs-collector) should be stopped, let's see..."
docker ps