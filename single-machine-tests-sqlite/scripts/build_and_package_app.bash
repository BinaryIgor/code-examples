#!/bin/bash
set -euo pipefail

echo "Building single app..."

export APP="single-app"
bash build_and_package.bash

echo "App is packaged and ready for deploy!"