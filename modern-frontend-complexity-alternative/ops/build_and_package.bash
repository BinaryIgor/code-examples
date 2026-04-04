#!/bin/bash
set -euo pipefail

app="modern-frontend-complexity-alternative"
tag="${TAG:-latest}"
tagged_image="${app}:${tag}"

echo "Creating package directory in dist directory for ${tagged_image}"
echo "Preparing dist dir"

cd ..

rm -r -f dist
mkdir dist

echo "Building static resources"

dev_resources=src/main/resources
dev_static_resources="$dev_resources/static"
package_resources=dist/src/main/resources
package_static_resources="$package_resources/static"

mkdir -p "$package_static_resources"

bundle_hash=$(openssl rand -hex 8)

input_css_path="$dev_static_resources/input.css"
hashed_css_file="styles_$bundle_hash.css"
output_css_path="$package_static_resources/$hashed_css_file"
export stylesPath=$output_css_path

npx @tailwindcss/cli --minify -i "${input_css_path}" -o "${output_css_path}"

cp -r "$dev_static_resources/lib" "$package_static_resources/lib"
cp -r "$dev_resources/templates" "$package_resources/templates"
cp -r "$dev_resources/application.yaml" "$package_resources/application.yaml"

components_file="components_${bundle_hash}.js"
export CSS_PATH="/${hashed_css_file}"
export COMPONENTS_PATH="/${components_file}"
envsubst '${CSS_PATH} ${COMPONENTS_PATH}' < "$dev_resources/application-prod.yaml" > "$package_resources/application.yaml"

cp "$dev_resources/messages.properties" "$package_resources/messages.properties"

export COMPONENTS_INPUT_DIR="${dev_static_resources}"
export COMPONENTS_OUTPUT_PATH="${package_static_resources}/${components_file}"

python3 ops/package_components.py

echo
echo "Static resources prepared, building Docker image"

docker build -t "$tagged_image" .

gzipped_image_path="dist/$app.tar.gz"

echo
echo "Image built, exporting it to $gzipped_image_path, this can take a while"

docker save "$tagged_image" | gzip > ${gzipped_image_path}

echo "Image exported, preparing scripts..."

export app=$app
export tag=$tag
export run_cmd="docker run -d --network host \\
--restart unless-stopped --name $app $tagged_image"

envsubst '${app} ${tag}' < ops/template_load_and_run_app.bash > dist/load_and_run_app.bash
envsubst '${app} ${run_cmd}' < ops/template_run_app.bash > dist/run_app.bash

echo "Package prepared."