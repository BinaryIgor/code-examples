#!/bin/bash
set -euo pipefail

regions=("fra" "lon" "tor" "syd")

for r in ${regions[@]}; do
  export REGION=$r
  bash build_and_package_static_app.bash
done