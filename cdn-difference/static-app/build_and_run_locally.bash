#!/bin/bash
set -e

container_name="cdn-nginx"

rm -rf target
mkdir -p target

export DOMAIN=localhost
envsubst '${DOMAIN}' < template_nginx.conf > target/nginx.conf

docker build . -t $container_name

docker stop $container_name || true
docker rm $container_name || true

docker run -v "$PWD/fake-certs/fullchain.pem:/certs/fullchain.pem" -v "$PWD/fake-certs/privkey.pem:/certs/privkey.pem" --network host \
    --name $container_name $container_name