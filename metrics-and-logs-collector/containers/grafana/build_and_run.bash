#!/bin/bash

container="grafana"

docker build . -t $container

docker rm $container

docker volume create "$container-volume"

# Grafana is available on port 3000 by default.
# We use network=host to have access to a Prometheus instance running on localhost:9090
docker run --network host -v "${container}-volume:/var/lib/grafana"  --name $container $container