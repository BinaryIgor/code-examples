#!/bin/bash
set -euo pipefail

apps=("nginx" "react" "server")
export ENV=prod

for app in ${apps[@]}; do
  export APP=$app
  bash build_and_package_app.bash
  echo
  echo "$app built!"
  echo
done

echo "All apps have been built!"
