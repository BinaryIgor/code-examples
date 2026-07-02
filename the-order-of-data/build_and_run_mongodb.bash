#!/bin/bash

cd mongodb

container_name="the-order-of-data-mongodb"
volume_dir="${MONGODB_VOLUME_DIR:-${HOME}/${container_name}_volume}"

docker build -t $container_name .

docker stop $container_name
docker rm $container_name

docker run -d --network host -v "${volume_dir}:/data/db" \
  -e "MONGO_INITDB_ROOT_USERNAME=mongo" \
  -e "MONGO_INITDB_ROOT_PASSWORD=mongo" \
  --memory "4G" --cpus "2" \
  --name $container_name $container_name
