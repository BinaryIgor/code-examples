#!/bin/bash

container_name="vector-embeddings-db"
volume_dir="${VOLUME_DIR:-${HOME}/vector_embeddings_db_volume}"

docker build . -t $container_name

docker rm $container_name

# Larger shared memory size is needed to create Hierarchical Navigable Small Worlds Index.
# What's more, if you want to have it built in a reasonable amount of time (like 20 minutes),
# you need to dedicate at least 4 CPUs and 8GB of RAM to this process and --shm-size=4g or even --shm-size=8g.
# It's better for IVFFlat index; the following resources are enough and should take less than 10 minutes to create for a table of ~ 1_000_000 rows
docker run -v "${volume_dir}:/var/lib/postgresql/data" -p "5432:5432" --shm-size=2g \
  --memory "8G" --cpus "2" \
  --name $container_name $container_name
