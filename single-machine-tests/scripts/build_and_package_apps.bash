#!/bin/bash
set -euo pipefail

echo "Building single db and app..."

echo "Single db..."
export APP=single-db
bash build_and_package.bash
echo

echo "Single app..."
export APP="single-app"
bash build_and_package.bash
echo

echo "Apps are packaged and ready for deploy!"