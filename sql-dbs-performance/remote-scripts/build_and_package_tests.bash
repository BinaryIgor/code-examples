#!/bin/bash
set -euo pipefail

cd ..
cd "tests"

echo "Building performance tests..."
bash build_and_package.bash

echo
echo "performance tests are built and ready to deploy!"