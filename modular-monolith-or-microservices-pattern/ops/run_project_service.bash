#!/bin/bash

. local.env

cd ..
cd project

container_name="modular-pattern-project-service"

docker stop $container_name
docker rm $container_name

docker run --network host \
  -e PROJECT_DB_URL -e PROJECT_DB_USERNAME -e PROJECT_DB_PASSWORD \
  --name $container_name $container_name