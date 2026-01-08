#!/bin/bash
set -euo pipefail

app="mysql-performance"
app_dir="mysql"

tag="${TAG:-latest}"
tagged_image="${app}:${tag}"
volume_dir="/home/ops/${app}_volume"
volume="$volume_dir:/var/lib/mysql"

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
  -e \"MYSQL_ROOT_PASSWORD=performance\" -e \"MYSQL_DATABASE=performance\" \\
  --shm-size="1G" \\
  --network host -v \"${volume}\" --name $app $tagged_image \\
  --innodb_buffer_pool_size=12G --innodb_redo_log_capacity=2G --transaction-isolation='READ-COMMITTED'"

cd ..
envsubst '${app} ${tag}' < remote-scripts/template_load_and_run_app.bash > "$app_dir/dist/load_and_run_app.bash"
envsubst '${app} ${run_cmd}' < remote-scripts/template_run_app.bash > "$app_dir/dist/run_app.bash"

echo "Package prepared."