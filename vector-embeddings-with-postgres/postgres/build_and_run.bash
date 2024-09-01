#!/bin/bash

container_name="vector-embeddings-db"
volume_dir="${VOLUME_DIR:-${HOME}/vector_embeddings_db_volume}"

docker build . -t $container_name

docker rm $container_name

docker run -v "${volume_dir}:/var/lib/postgresql/data" -p "5432:5432" --shm-size=1g \
  --memory "8G" --cpus "2" \
  --name $container_name $container_name
