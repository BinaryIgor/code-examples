#!/bin/bash

cd mongodb

container_name="json-dbs-mongodb"
volume_dir="${MONGODB_VOLUME_DIR:-${HOME}/${container_name}_volume}"

docker build -t $container_name .

docker stop $container_name
docker rm $container_name

docker run -d --network host -v "${volume_dir}:/data/db" \
  -e "MONGO_INITDB_DATABASE=json" \
  -e "MONGO_INITDB_ROOT_USERNAME=json" \
  -e "MONGO_INITDB_ROOT_PASSWORD=json" \
  --memory "16G" --cpus "8" \
  --name $container_name $container_name \
  --wiredTigerCacheSizeGB 12