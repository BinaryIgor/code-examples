#!/bin/bash
set -euo pipefail

cd ..
. "config_prod.env"
cd react

remote_host="$DEPLOY_USER@$SPA_DOMAIN"
remote_site_dir=$STATIC_PATH
package_path="dist"

ssh $remote_host "mkdir -p /tmp/react; sudo mkdir -p $remote_site_dir"
scp -r $package_path/* $remote_host:/tmp/react
ssh $remote_host "sudo rm -f -r $remote_site_dir/*; sudo mv /tmp/react/* $remote_site_dir"