#!/bin/bash
set -euo pipefail

app="htmx-mpa-vs-react-spa"
tag="${TAG:-latest}"
tagged_image="${app}:${tag}"

echo "Creating package in dist directory for $tagged_image image..."
echo "Preparing dist dir..."

rm -r -f dist
mkdir dist

. "config_${ENV}.env"

echo "Building static resources..."

package_static_resources=dist/static
bundle_hash=$(openssl rand -hex 8)

input_css_path=static/styles.css
hashed_css="styles_$bundle_hash.css"
output_css_path=$package_static_resources/$hashed_css
export stylesPath=$output_css_path

./tailwindcss --minify -i $input_css_path -o $output_css_path

cp -r static/js $package_static_resources/js
cp -r static/templates $package_static_resources/templates

echo
echo "Building image..."

echo "Building jar locally first..."
cp pom.xml dist/pom.xml
cp -r src dist/src
mv dist/static dist/src/main/resources

cd dist
mvn clean package
cd ..

echo
echo "Jar built! Packing it in Docker..."
echo

docker build -t ${tagged_image} .

rm -r dist/target dist/src
rm dist/pom.xml

gzipped_image_path="dist/$app.tar.gz"

echo "Image built, exporting it to $gzipped_image_path, this can take a while..."

docker save ${tagged_image} | gzip > ${gzipped_image_path}

echo "Image exported, preparing scripts..."

spring_profile_env="SPRING_PROFILES_ACTIVE=$ENV"
css_path_env="CSS_PATH=/$hashed_css"

export app=$app
export tag=$tag
export run_cmd="export AUTH_TOKEN_KEY=\$(cat $SECRETS_PATH/auth-token-key.txt)

docker run -d --network host \\
-e $spring_profile_env -e $css_path_env \\
-e AUTH_TOKEN_KEY -e DB_PASSWORD \\
--name $app $tagged_image"

envsubst '${app} ${tag}' < scripts/template_load_and_run_app.bash > dist/load_and_run_app.bash
envsubst '${app} ${run_cmd}' < scripts/template_run_app.bash > dist/run_app.bash

echo "Package prepared."