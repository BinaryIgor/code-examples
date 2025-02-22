#!/bin/bash
set -euo pipefail

export app="htmx-mpa-vs-react-spa-nginx-spa"
export tag="${TAG:-latest}"
tagged_image="${app}:${tag}"

echo "Creating package in dist directory for $tagged_image image..."
echo "Preparing dist dir..."

rm -r -f dist
mkdir dist

cd ..
. "config_${ENV}.env"

cd nginx-spa
. "config_${ENV}.env"

echo "Building docker image..."

export SERVER_PORT=${SERVER_PORT:-8080}
export DOMAIN=${SPA_DOMAIN}

envsubst '${SERVER_PORT} ${DOMAIN}' < template_nginx.conf > dist/nginx.conf

docker build . -t ${tagged_image}

gzipped_image_path="dist/$app.tar.gz"

echo "Image built, exporting it to $gzipped_image_path, this can take a while..."

docker save ${tagged_image} | gzip > ${gzipped_image_path}

if [ $ENV = 'local' ]; then
  cp -r ../scripts/fake-certs dist/fake-certs
  CERTS_VOLUME="-v $PWD/dist/fake-certs/fullchain.pem:/etc/certs/live/${DOMAIN}/fullchain.pem  -v $PWD/dist/fake-certs/privkey.pem:/etc/certs/live/${DOMAIN}/privkey.pem"
else
  CERTS_VOLUME="-v ${CERTS_VOLUME}"
fi
STATIC_RESOURCES_VOLUME="${STATIC_PATH}:/usr/share/nginx/site:ro"

export docker_run_params="--network host \\
${CERTS_VOLUME} \\
-v ${STATIC_RESOURCES_VOLUME} \\
--restart ${DOCKER_RESTART}"

export app=$app
export tag=$tag
export run_cmd="docker run -d $docker_run_params --name $app $tagged_image"

cd ..
envsubst '${app} ${tag}' < scripts/template_load_and_run_app.bash > nginx-spa/dist/load_and_run_app.bash
envsubst '${app} ${run_cmd}' < scripts/template_run_app.bash > nginx-spa/dist/run_app.bash

echo "Package prepared."