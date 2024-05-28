#!/bin/bash

. local.env

cd ..
cd modular-monolith

container_name="modular-pattern-monolith"

docker stop $container_name
docker rm $container_name

docker run --network host \
  -e USER_DB_URL -e USER_DB_USERNAME -e USER_DB_PASSWORD \
  -e PROJECT_DB_URL -e PROJECT_DB_USERNAME -e PROJECT_DB_PASSWORD \
  --name $container_name $container_name