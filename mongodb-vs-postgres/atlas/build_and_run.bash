#!/bin/bash

container_name=mongodb-atlas
volume_dir="${MYSQL_VOLUME_DIR:-${HOME}/${container_name}_volume}"

docker build -t $container_name .

docker stop $container_name
docker rm $container_name

docker run -d --network host \
  -v./init:/docker-entrypoint-initdb.d -v db:/data/db -v configdb:/data/configdb -v mongot:/data/mongot \
  -e MONGODB_INITDB_ROOT_USERNAME=user \
  -e MONGODB_INITDB_ROOT_PASSWORD=pass \
  --name $container_name $container_name