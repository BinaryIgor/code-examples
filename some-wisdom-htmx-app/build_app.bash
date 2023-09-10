#!/bin/bash
set -e

echo "Compiling js..."
rm -r -f dist
tsc 

echo "Creating a hash for styles and js..."
assets_hash=$(date +%s)

echo "Preparing css..."
npx tailwindcss -i ./assets/style.css -o "./dist/assets/style-${assets_hash}.css"

# TODO: minification of assets!
echo "Moving assets.."

cp -r assets/db dist/assets/db
cp -r assets/index.js "dist/assets/index-${assets_hash}.js"

echo "$assets_hash" > dist/assets_hash.txt

echo "App is ready!"