#!/bin/bash
set -euo pipefail

app="postgres-db"
tag="latest"
tagged_image="${app}:${tag}"

echo "Building $app..."

rm -r -f dist
mkdir dist

echo "Building image..."

docker build . -t ${tagged_image}

gzipped_image_path="dist/$app.tar.gz"

echo "Image built, exporting it to $gzipped_image_path, this can take a while..."

docker save ${tagged_image} | gzip > ${gzipped_image_path}

echo "Image exported, preparing scripts..."

cd ..

data_volume="/home/deploy/postgres-volume:/var/lib/postgresql/data"
export run_cmd="docker run -d --network host -v ${data_volume} --restart unless-stopped --name $app $tagged_image"

export app=$app
export tag=$tag

envsubst '${app} ${tag}' < "scripts/templates/template_load_and_run_app.bash" > $app/dist/load_and_run_app.bash
envsubst '${app} ${run_cmd}' <  "scripts/templates/template_run_app.bash"> $app/dist/run_app.bash

echo "Package prepared."