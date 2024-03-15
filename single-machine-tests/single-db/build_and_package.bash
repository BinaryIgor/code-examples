#!/bin/bash
set -euo pipefail

app="single-db"
tag="${TAG:-latest}"
tagged_image="${app}:${tag}"
volume="/mnt/single_machine_volume/data:/var/lib/postgresql/data"

echo "Creating package in target directory for $tagged_image image..."
echo "Preparing target dir..."

rm -r -f target
mkdir target

echo "Building image..."

docker build . -t ${tagged_image}

gzipped_image_path="target/$app.tar.gz"

echo "Image built, exporting it to $gzipped_image_path, this can take a while..."

docker save ${tagged_image} | gzip > ${gzipped_image_path}

echo "Image exported, preparing scripts..."

export app=$app
export tag=$tag
export run_cmd="docker run -d --network host -v ${volume} --restart unless-stopped --name $app $tagged_image"

cd ..
envsubst '${app} ${tag}' < scripts/template_load_and_run_app.bash > $app/target/load_and_run_app.bash
envsubst '${app} ${run_cmd}' < scripts/template_run_app.bash > $app/target/run_app.bash

echo "Package prepared."