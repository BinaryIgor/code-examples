#!/bin/bash
set -euo pipefail

app="db-backup-job"
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

echo 'export DB_NAME=backup_db
export DB_USER=backup
export MAX_LOCAL_BACKUPS=10
export MAX_DO_BACKUPS=50
export UPLOAD_TO_DO_SPACES=true
export DO_REGION=fra1
export DO_SPACES_BUCKET=binaryigor
export DO_SPACES_BUCKET_FOLDER=db-backups
export DO_SPACES_KEY=$(cat /home/deploy/.secrets/do-spaces-key.txt)
export DO_SPACES_SECRET=$(cat /home/deploy/.secrets/do-spaces-secret.txt)' > $app/dist/.env

db_backups_path="/home/deploy/db-backups"
job_metrics_path="/home/deploy/job-metrics"
docker_create_params="--network host \\
-e DB_NAME -e DB_USER -e MAX_LOCAL_BACKUPS -e MAX_DO_BACKUPS \\
-e UPLOAD_TO_DO_SPACES \\
-e DO_REGION -e DO_SPACES_BUCKET -e DO_SPACES_BUCKET_FOLDER \\
-e DO_SPACES_KEY -e DO_SPACES_SECRET \\
-v ${db_backups_path}:/backups -v ${job_metrics_path}:/job-metrics"

export create_cmd=". .env

docker create ${docker_create_params} --name $app $tagged_image"

export app=$app
export tag=$tag

envsubst '${app} ${tag}' < scripts/templates/template_load_and_create_app.bash > $app/dist/load_and_create_app.bash
envsubst '${app} ${create_cmd}' < scripts/templates/template_create_app.bash > $app/dist/create_app.bash

echo "Package prepared."