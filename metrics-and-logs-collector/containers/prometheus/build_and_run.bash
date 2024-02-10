#!/bin/bash

container="prometheus"

docker rm $container

docker build . -t $container

docker volume create "$container-volume"

docker run -d --memory "250M" --cpus "1" \
  --network host -v "$container-volume:/prometheus" \
  --name $container $container  \
  --storage.tsdb.retention.time=30d  \
  --config.file=/etc/prometheus/prometheus.yml