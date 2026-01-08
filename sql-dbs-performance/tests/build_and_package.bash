#!/bin/bash
set -euo pipefail

app="sql-db-performance-tests"
app_dir="tests"
tag="${TAG:-latest}"
tagged_image="${app}:${tag}"
dist_dir="dist"

echo "Creating package in $dist_dir directory for $tagged_image image..."
echo "Preparing $dist_dir dir..."

rm -r -f ${dist_dir}
mkdir ${dist_dir}

echo "Building image..."

docker build . -t "${tagged_image}"

gzipped_image_path="${dist_dir}/$app.tar.gz"

echo "Image built, exporting it to $gzipped_image_path, this can take a while..."

docker save "${tagged_image}" | gzip > ${gzipped_image_path}

echo "Image exported, preparing scripts..."

cd ..
export app=$app
export tag=$tag
envsubst '${app} ${tag}' < remote-scripts/template_load_app.bash > $app_dir/$dist_dir/load_app.bash

echo "Package prepared."