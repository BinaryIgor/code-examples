#!/bin/bash
set -e

rollup -c

npx tailwindcss -i ./style.css -o ./dist/style.css --minify
