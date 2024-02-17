#!/bin/bash

containers=("postgres-db" "prometheus" "some-custom-app" "logs-browser")

echo "About to run containers: ${containers[@]}"
echo

for c in ${containers[@]}; do
  echo "Starting $c container..."
  cd $c
  bash build_and_run.bash
  cd ..
  echo
done

echo
echo "All containers should be running!"