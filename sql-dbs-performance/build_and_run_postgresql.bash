#!/bin/bash

cd postgresql

container_name="postgresql-performance"
volume_dir="${POSTGRESQL_VOLUME_DIR:-${HOME}/${container_name}_volume}"

docker build . -t $container_name

docker stop $container_name
docker rm $container_name

# work_mem is per sort / per hash / per node, not per query. A single query can use it multiple times
docker run -d -v "${volume_dir}:/var/lib/postgresql" --network host \
  -e "POSTGRES_PASSWORD=performance" \
  -e "POSTGRES_DB=performance" \
  --memory "16G" --cpus "8" --shm-size="1G" \
  --name $container_name $container_name \
  -c shared_buffers=4GB \
  -c work_mem=64MB \
  -c effective_cache_size=12GB
