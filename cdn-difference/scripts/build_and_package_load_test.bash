#!/bin/bash
set -euo pipefail

echo "Building load test..."
export APP=load-test
bash build_and_package.bash
echo

echo "Load Test is packaged and ready for deploy!"