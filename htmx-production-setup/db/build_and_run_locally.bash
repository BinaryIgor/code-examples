#!/bin/bash
set -euo pipefail

export ENV=local
export SKIP_IMAGE_EXPORT="true"
bash build_and_package.bash

cd dist
bash run_app.bash