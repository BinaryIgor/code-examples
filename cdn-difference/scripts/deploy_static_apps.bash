#!/bin/bash
set -euo pipefail

cd ..
. config.env
cd scripts

regions=("fra" "lon" "tor" "syd")

for r in ${regions[@]}; do
  export REGION=$r
  export DEPLOY_HOST="static-$REGION.$ROOT_DOMAIN"
  bash deploy_static_app.bash
done