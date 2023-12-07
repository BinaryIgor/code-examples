#!/bin/bash
set -e

echo "Preparing css..."
npx tailwindcss -i ./style.css -o ./dist/style.css --minify

echo "css is ready, starting the app!"

node .