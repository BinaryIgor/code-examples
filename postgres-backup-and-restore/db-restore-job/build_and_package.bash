#!/bin/bash
set -euo pipefail

app="db-restore-job"
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

db_backups_path="/home/deploy/db-backups"
docker_create_params="--network host \\
-e DB_NAME -e DB_USER \\
-v ${db_backups_path}:/backups"

export create_cmd="export DB_NAME=\"\${DB_NAME:-backup_db}\"
export DB_USER=\"\${DB_USER:-backup}\"
docker create ${docker_create_params} --name $app $tagged_image"

export app=$app
export tag=$tag

envsubst '${app} ${tag}' < scripts/templates/template_load_and_create_app.bash > $app/dist/load_and_create_app.bash
envsubst '${app} ${create_cmd}' < scripts/templates/template_create_app.bash > $app/dist/create_app.bash

echo "Package prepared."