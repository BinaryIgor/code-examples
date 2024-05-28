#!/bin/bash

. local.env

cd ..
cd user

container_name="modular-pattern-user-service"

docker stop $container_name
docker rm $container_name

docker run --network host \
  -e USER_DB_URL -e USER_DB_USERNAME -e USER_DB_PASSWORD \
  --name $container_name $container_name