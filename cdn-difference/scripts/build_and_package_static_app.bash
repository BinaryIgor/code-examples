#!/bin/bash
set -euo pipefail

export APP="static-app"
echo "Building $APP of $REGION..."
bash build_and_package.bash
echo

echo "$APP of $REGION is packaged and ready for deploy!"