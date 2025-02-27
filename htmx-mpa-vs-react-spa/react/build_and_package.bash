#!/bin/bash
# for scripts/build_and_package_app.bash mostly
set -euo pipefail

npm ci
npm run build