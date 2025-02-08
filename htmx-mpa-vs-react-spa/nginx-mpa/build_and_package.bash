#!/bin/bash
set -euo pipefail

export app="htmx-mpa-nginx"
export tag="${TAG:-latest}"
tagged_image="${app}:${tag}"

echo "Creating package in dist directory for $tagged_image image..."
echo "Preparing dist dir..."

rm -r -f dist
mkdir dist

cd ..
. "config_${ENV}.env"

cd nginx-mpa
. "config_${ENV}.env"

echo "Building docker image..."

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

# TODO: fix
pre_run="bash update_app_url_pre_start.bash"
if [ $ENV = 'prod' ]; then
  pre_run_action="sudo cp reload_nginx_config.sh /etc/letsencrypt/renewal-hooks/post/reload_nginx_config.sh
sudo chmod +x /etc/letsencrypt/renewal-hooks/post/reload_nginx_config.sh"
else
  pre_run_action=""
fi

export pre_run_cmd="$pre_run_action"

export docker_run_params="--network host \\
${CERTS_VOLUME} \\
--restart ${DOCKER_RESTART}"

export app=$app
export tag=$tag
export run_cmd="$pre_run_cmd
docker run -d $docker_run_params --name $app $tagged_image
"

cd ..
envsubst '${app} ${tag}' < scripts/template_load_and_run_app.bash > nginx-mpa/dist/load_and_run_app.bash
envsubst '${app} ${run_cmd}' < scripts/template_run_app.bash > nginx-mpa/dist/run_app.bash

echo "Package prepared."