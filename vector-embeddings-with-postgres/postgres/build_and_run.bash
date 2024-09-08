#!/bin/bash

container_name="vector-embeddings-db"
volume_dir="${VOLUME_DIR:-${HOME}/vector_embeddings_db_volume}"

docker build . -t $container_name

docker rm $container_name

# Larger shared memory size need to create Hierarchical Navigable Small Worlds Index.
# What's more, if you want to have it built in a reasonable amount of time (like 20 minutes),
# you need to dedicate at least 4 CPUS and 8GB of RAM to this process
docker run -v "${volume_dir}:/var/lib/postgresql/data" -p "5432:5432" --shm-size=8g \
  --memory "8G" --cpus "2" \
  --name $container_name $container_name
