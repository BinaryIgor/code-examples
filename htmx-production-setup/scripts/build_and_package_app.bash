#!/bin/bash
set -euo pipefail

app=$APP
app_dir="${APP_DIR:-$APP}"

echo "Building $app with ${ENV} env profile..."

cd ..
cd $app_dir
bash build_and_package.bash
