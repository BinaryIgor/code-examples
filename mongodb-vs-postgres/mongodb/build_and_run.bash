#!/bin/bash
container_name=mongodb-vs-postgres-mongodb
volume_dir="${MONGODB_VOLUME_DIR:-${HOME}/${container_name}_volume}"

docker build -t $container_name .

docker stop $container_name
docker rm $container_name

docker run -d --network host \
  -v $volume_dir:/data/db \
  -e MONGO_INITDB_DATABASE=experiments \
  -e MONGO_INITDB_ROOT_USERNAME=user \
  -e MONGO_INITDB_ROOT_PASSWORD=pass \
  --name $container_name $container_name