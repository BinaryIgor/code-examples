#!/bin/bash
set -euo pipefail

export app="htmx-nginx"
export tag="${TAG:-latest}"
tagged_image="${app}:${tag}"

echo "Creating package in dist directory for $tagged_image image..."
echo "Preparing dist dir..."

rm -r -f dist
mkdir dist
mkdir dist/conf

cd ..
. "config_${ENV}.env"

cd nginx
. "config_${ENV}.env"

export HTTP_PORT=${HTTP_PORT:-80}
export HTTPS_PORT=${HTTPS_PORT:-443}
export APP_URL="${APP_URL:-http://0.0.0.0:8080}"

envsubst '${HTTP_PORT} ${HTTPS_PORT} ${DOMAIN} ${APP_URL}' < template_nginx.conf > dist/conf/default.conf
envsubst '${HTTP_PORT} ${HTTPS_PORT} ${DOMAIN}' < template_nginx.conf > dist/template_nginx_app.conf

export nginx_container="htmx-nginx"
# check if both proxying and proxied app are working properly
export app_health_check_url="http://0.0.0.0:${HTTP_PORT}/actuator/health"

envsubst '${nginx_container} ${app_health_check_url}' < template_update_app_url.bash > dist/update_app_url.bash

cp update_app_url_pre_start.bash dist/update_app_url_pre_start.bash

envsubst '${app_health_check_url}' < template_check_proxied_app.bash > dist/check_proxied_app.bash

envsubst '${nginx_container}' < template_reload_nginx_config.sh > dist/reload_nginx_config.sh

echo "Scripts prepared, building docker image..."

docker build . -t ${tagged_image}

gzipped_image_path="dist/$app.tar.gz"

echo "Image built, exporting it to $gzipped_image_path, this can take a while..."

docker save ${tagged_image} | gzip > ${gzipped_image_path}

if [ $ENV = 'local' ]; then
    cp -r fake-certs dist/fake-certs
    CERTS_VOLUME="-v $PWD/dist/fake-certs/fullchain.pem:/etc/certs/live/${DOMAIN}/fullchain.pem  -v $PWD/dist/fake-certs/privkey.pem:/etc/certs/live/${DOMAIN}/privkey.pem"
else
    CERTS_VOLUME="-v ${CERTS_VOLUME}"
fi

pre_run="bash update_app_url_pre_start.bash $APP_URL_FILE_PATH"
if [ $ENV = 'prod' ]; then
    pre_run_action="chmod -x reload_nginx_config.sh
sudo cp reload_nginx_config.sh /etc/letsencrypt/renewal-hooks/post/reload_nginx_config.sh"
else
    pre_run_action=""
fi

export pre_run_cmd="$pre_run_action
bash update_app_url_pre_start.bash $APP_URL_FILE_PATH"

export docker_run_params="--network host \\
    -v ${CONFIG_VOLUME} \\
    ${CERTS_VOLUME} \\
    --restart ${DOCKER_RESTART}"
    
export post_run_cmd="bash check_proxied_app.bash"

export app=$app
export tag=$tag
export run_cmd="$pre_run_cmd
docker run -d $docker_run_params --name $app $tagged_image
$post_run_cmd"

cd ..
envsubst '${app} ${tag}' < scripts/template_load_and_run_app.bash > nginx/dist/load_and_run_app.bash
envsubst '${app} ${run_cmd}' < scripts/template_run_app.bash > nginx/dist/run_app.bash

echo "Package prepared."