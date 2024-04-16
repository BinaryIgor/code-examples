#!/bin/bash
set -euo pipefail

app="htmx-production-setup-db"
tag="latest"
tagged_image="${app}:${tag}"

echo "Building $app..."

rm -r -f dist
mkdir dist

cd ..
. "config_${ENV}.env"

cd db
. "config_${ENV}.env"

echo "Building image..."

docker build . -t ${tagged_image}

gzipped_image_path="dist/$app.tar.gz"

echo "Image built, exporting it to $gzipped_image_path, this can take a while..."

docker save ${tagged_image} | gzip > ${gzipped_image_path}

echo "Image exported, preparing scripts..."

cd ..

data_volume="${DATABASE_VOLUME}:/var/lib/postgresql/data"
export run_cmd="docker run -d --network host -v ${data_volume} --restart ${DOCKER_RESTART} --name $app $tagged_image"

export app=$app
export tag=$tag

envsubst '${app} ${tag}' < "scripts/template_load_and_run_app.bash" > db/dist/load_and_run_app.bash
envsubst '${app} ${run_cmd}' < "scripts/template_run_app.bash"> db/dist/run_app.bash

echo "Package prepared."