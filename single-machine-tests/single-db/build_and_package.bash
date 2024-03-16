#!/bin/bash
set -euo pipefail

app="single-db"
tag="${TAG:-latest}"
tagged_image="${app}:${tag}"
# TODO: better naming strategy
volume="-v /mnt/single_machine_small_volume/data:/var/lib/postgresql/data"

echo "Creating package in dist directory for $tagged_image image..."
echo "Preparing dist dir..."

rm -r -f dist
mkdir dist

echo "Building image..."

docker build . -t ${tagged_image}

gzipped_image_path="dist/$app.tar.gz"

echo "Image built, exporting it to $gzipped_image_path, this can take a while..."

docker save ${tagged_image} | gzip > ${gzipped_image_path}

echo "Image exported, preparing scripts..."

export app=$app
export tag=$tag
export run_cmd="docker run -d --network host ${volume} --restart unless-stopped --name $app $tagged_image"

cd ..
envsubst '${app} ${tag}' < scripts/template_load_and_run_app.bash > $app/dist/load_and_run_app.bash
envsubst '${app} ${run_cmd}' < scripts/template_run_app.bash > $app/dist/run_app.bash

echo "Package prepared."