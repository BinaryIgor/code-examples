#!/bin/bash
set -euo pipefail

regions=("fra" "lon" "syd") 

export DOMAIN="binaryigor.com"

for r in ${regions[@]}; do
  export REGION=$r
  export DEPLOY_HOST="static-$REGION.$DOMAIN"
  bash deploy_static_app.bash
done