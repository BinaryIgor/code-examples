#!/bin/bash

container_name="modular-monolith-with-independent-deployments-db"
db_volume_path=${1:-"/home/igor/$container_name"}

docker build . -t $container_name

docker stop $container_name || true
docker rm $container_name || true

# -v "$db_volume_path:/var/lib/postgresql/data" \
docker run -p "5555:5432" \
  --memory "2000M" \
  --cpus "2" \
  --name $container_name $container_name
#  -c log_statement=all
