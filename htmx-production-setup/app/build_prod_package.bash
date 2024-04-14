#!/bin/bash

rm -rf dist
mkdir -p dist

cp -r src dist/src

package_static_resources=dist/src/main/resources/static
bundle_hash=$(openssl rand -hex 8)

input_css_path=static/styles.css
output_css_path="$package_static_resources/styles_$bundle_hash.css"
export stylesPath=$output_css_path

./tailwindcss --minify -i $input_css_path -o $output_css_path

cp -r static/lib $package_static_resources/lib
cp static/base.js $package_static_resources/base_$bundle_hash.js

components=$(cat static/components.js)

components_with_hashed_base_import=$(echo "$components" | sed "s/base.js/base_$bundle_hash.js/")

echo "$components_with_hashed_base_import" > $package_static_resources/components_$bundle_hash.js

cp static/index.js $package_static_resources/index_$bundle_hash.js