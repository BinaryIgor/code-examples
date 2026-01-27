#!/bin/bash

cd postgresql

container_name="json-dbs-postgresql"
volume_dir="${POSTGRESQL_VOLUME_DIR:-${HOME}/${container_name}_volume}"

docker build . -t $container_name

docker stop $container_name
docker rm $container_name

# c synchronous_commit=off \
#  -c wal_writer_delay=33ms
docker run -d -v "${volume_dir}:/var/lib/postgresql" --network host \
  -e "POSTGRES_DB=json" \
  -e "POSTGRES_USER=json" \
  -e "POSTGRES_PASSWORD=json" \
  --memory "16G" --cpus "8" --shm-size="1G" \
  --name $container_name $container_name \
  -c shared_buffers=4GB \
  -c work_mem=128MB \
  -c effective_cache_size=12GB