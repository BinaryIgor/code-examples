#!/bin/bash
set -euo pipefail

echo "Deploying load tests to $DEPLOY_TEST_HOSTS..."

for h in ${DEPLOY_TEST_HOSTS[@]}; do
    echo "Deploy load test to $h host..."
    export DEPLOY_TEST_HOST="$h"
    bash deploy_load_test.bash
    echo "Load test deployed to $h host!"
done