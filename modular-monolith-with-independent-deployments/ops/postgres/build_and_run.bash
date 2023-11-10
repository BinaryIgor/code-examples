#!/bin/bash

container_name="modular-monolith-with-independent-deployments-db"

docker build . -t $container_name

docker stop $container_name || true
docker rm $container_name || true

docker run -p "5555:5432" \
  --memory "2000M" \
  --cpus "2" \
  --name $container_name $container_name
