#!/bin/bash

container_name="postgres-indexes"
db_volume_path=${1:-"${HOME}/$container_name"}

docker build . -t $container_name

docker stop $container_name || true
docker rm $container_name || true

# remove memory and cpus limits during insert to speed up the process!
docker run -p "5555:5432" \
  -v "$db_volume_path:/var/lib/postgresql/data" \
  --memory "1000M" \
  --cpus "1" \
  --name $container_name $container_name
#  -c log_statement=all