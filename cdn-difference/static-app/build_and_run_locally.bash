#!/bin/bash
set -e

container_name="static-app"

rm -rf dist
mkdir -p dist

export domain=localhost
envsubst '${domain}' < template_nginx.conf > dist/nginx.conf

docker build --build-arg nginx_conf_dir="dist" . -t $container_name

docker stop $container_name || true
docker rm $container_name || true

docker run -v "$PWD/fake-certs/fullchain.pem:/certs/fullchain.pem" -v "$PWD/fake-certs/privkey.pem:/certs/privkey.pem" --network host \
    --name $container_name $container_name