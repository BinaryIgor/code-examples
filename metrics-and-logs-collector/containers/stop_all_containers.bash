#!/bin/bash

containers=("postgres-db" "prometheus" "some-app")

echo "About to stop containers: ${containers[@]}"
echo

for c in ${containers[@]}; do
  docker stop $c
done

echo
echo "All containers should be stopped, let's see..."
docker ps