#!/bin/bash
set -e

rollup -c

tailwindcss -i ./style.css -o ./dist/style.css --watch
