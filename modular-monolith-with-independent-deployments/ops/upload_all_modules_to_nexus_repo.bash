#!/bin/bash
set -e

all_modules=("commons/spring-parent" "commons/contracts"
  "modules/budget" "modules/campaign" "modules/inventory")

for m in ${all_modules[@]}; do
  bash upload_module_to_nexus_repo.bash $m
done