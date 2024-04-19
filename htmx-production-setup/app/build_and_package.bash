#!/bin/bash
set -euo pipefail

app="htmx-production-setup-app"
tag="${TAG:-latest}"
tagged_image="${app}:${tag}"
build_in_docker=${BUILD_IN_DOCKER:-false}

echo "Creating package in dist directory for $tagged_image image..."
echo "Preparing dist dir..."

rm -r -f dist
mkdir dist

cd ..
. "config_${ENV}.env"

cd app
. "config_${ENV}.env"

echo "Building static resources..."

package_static_resources=dist/static
bundle_hash=$(openssl rand -hex 8)

input_css_path=static/styles.css
hashed_css="styles_$bundle_hash.css"
output_css_path=$package_static_resources/$hashed_css
export stylesPath=$output_css_path

./tailwindcss --minify -i $input_css_path -o $output_css_path

cp -r static/lib $package_static_resources/lib

hashed_components_base="base_$bundle_hash.js"
cp static/base.js $package_static_resources/$hashed_components_base

components=$(cat static/components.js)

components_with_hashed_base_import=$(echo "$components" | sed "s/base.js/$hashed_components_base/")

hashed_components="components_$bundle_hash.js"
echo "$components_with_hashed_base_import" > $package_static_resources/$hashed_components

hashed_index_js="index_$bundle_hash.js"
cp static/index.js $package_static_resources/$hashed_index_js

echo
echo "Building image..."

if [ $build_in_docker = 'true' ]; then
  echo "Building jar inside Docker, it might take a little bit longer..."
  docker build -f DockerfileWithBuild -t ${tagged_image} .
else
  echo "Building jar locally first..."
  cp pom.xml dist/pom.xml
  cp -r src dist/src
  mv dist/static dist/src/main/resources

  cd dist
  mvn clean package
  cd ..

  echo
  echo "Jar built! packing it in Docker..."
  echo

  docker build -t ${tagged_image} .
fi

rm -r dist/target dist/src
rm dist/pom.xml

gzipped_image_path="dist/$app.tar.gz"

echo "Image built, exporting it to $gzipped_image_path, this can take a while..."

docker save ${tagged_image} | gzip > ${gzipped_image_path}

echo "Image exported, preparing scripts..."

server_port=$(shuf -i 10000-20000 -n 1)

app_url="http://0.0.0.0:$server_port"
echo $app_url > "dist/current_url.txt"

server_port_env="SERVER_PORT=$server_port"
spring_profile_env="SPRING_PROFILES_ACTIVE=$ENV"
css_path_env="CSS_PATH=/$hashed_css"
components_path_env="COMPONENTS_PATH=/$hashed_components"
index_js_path_env="INDEX_JS_PATH=/$hashed_index_js"

export app=$app
export tag=$tag
export run_cmd="export AUTH_TOKEN_KEY=\$(cat $SECRETS_PATH/auth-token-key.txt)
export DB_PASSWORD=\$(cat $SECRETS_PATH/db-password.txt)

docker run -d --network host -e $server_port_env \\
-e $spring_profile_env -e $css_path_env -e $components_path_env -e $index_js_path_env \\
-e AUTH_TOKEN_KEY -e DB_PASSWORD \\
--restart ${DOCKER_RESTART} --name $app $tagged_image"

export upstream_nginx_dir=$UPSTREAM_NGINX_DIR
export app_url=$app_url
export app_health_check_url="$app_url/actuator/health"

cd ..

envsubst '${app} ${tag}' < scripts/template_load_and_run_app.bash > app/dist/load_and_run_app.bash
envsubst '${app} ${run_cmd} ${app_url} ${app_health_check_url} ${upstream_nginx_dir}' < scripts/template_run_zero_downtime_app.bash  > app/dist/run_app.bash

echo "Package prepared."