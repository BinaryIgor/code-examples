#!/bin/bash
set -euo pipefail

regions=("fra" "lon" "tor" "syd")

cd ..
. config.env
cd scripts

export DOMAIN_EMAIL=$DOMAINS_EMAIL

for r in ${regions[@]}; do
  export DOMAIN="static-$r.$ROOT_DOMAIN"
  echo "Setting up https cert for $DOMAIN..."
  bash set_up_https_cert.bash
  echo
done