#!/bin/bash
set -euo pipefail

app="static-app"
region=$REGION
tag="${TAG:-latest}"
tagged_image="${app}:${tag}"
export domain="static-${region}.${ROOT_DOMAIN}"

echo "Creating package in dist directory for $tagged_image image..."
echo "Preparing dist dir..."

dist_dir="dist/$region"

rm -r -f ${dist_dir}
mkdir -p ${dist_dir}

envsubst '${domain}' < template_nginx.conf > "${dist_dir}/nginx.conf"

echo "Building image..."

docker build --build-arg nginx_conf_dir="${dist_dir}" . -t ${tagged_image}

gzipped_image_path="${dist_dir}/$app.tar.gz"

echo "Image built, exporting it to $gzipped_image_path, this can take a while..."

docker save ${tagged_image} | gzip > ${gzipped_image_path}

echo "Image exported, preparing scripts..."

export certs_dir="/etc/letsencrypt/live/$domain"
export app=$app
export tag=$tag
export run_cmd="docker run -d --network host \\
    -v \"$certs_dir/fullchain.pem:/certs/fullchain.pem\" -v \"$certs_dir/privkey.pem:/certs/privkey.pem\" \\
    --restart unless-stopped --name $app $tagged_image"

cd ..
envsubst '${app} ${tag}' < scripts/template_load_and_run_app.bash > $app/${dist_dir}/load_and_run_app.bash
envsubst '${app} ${run_cmd}' < scripts/template_run_app.bash > $app/${dist_dir}/run_app.bash

echo "Package prepared."