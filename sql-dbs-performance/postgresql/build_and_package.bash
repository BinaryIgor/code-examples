#!/bin/bash
set -euo pipefail

app="postgresql-performance"
app_dir="postgresql"

tag="${TAG:-latest}"
tagged_image="${app}:${tag}"
volume_dir="/home/ops/${app}_volume"
volume="$volume_dir:/var/lib/postgresql"

echo "Creating package in dist directory for $tagged_image image..."
echo "Preparing dist dir..."

rm -r -f dist
mkdir dist

echo "Building image..."

docker build . -t "$tagged_image"

gzipped_image_path="dist/$app.tar.gz"

echo "Image built, exporting it to $gzipped_image_path, this can take a while..."

docker save "$tagged_image" | gzip > "$gzipped_image_path"

echo "Image exported, preparing scripts..."

export app=$app
export tag=$tag
export run_cmd="docker run -d \\
  -e \"POSTGRES_USER=postgres\" -e \"POSTGRES_PASSWORD=performance\" -e \"POSTGRES_DB=performance\" \\
  --shm-size="1G" \\
  --network host -v \"${volume}\" --name $app $tagged_image \\
  -c shared_buffers=4GB -c work_mem=64MB -c effective_cache_size=12GB"

cd ..
envsubst '${app} ${tag}' < remote-scripts/template_load_and_run_app.bash > "$app_dir/dist/load_and_run_app.bash"
envsubst '${app} ${run_cmd}' < remote-scripts/template_run_app.bash > "$app_dir/dist/run_app.bash"

echo "Package prepared."