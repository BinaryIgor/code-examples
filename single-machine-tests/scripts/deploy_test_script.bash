#!/bin/bash
set -euo pipefail

deploy_user="test-machine"
remote_host="$deploy_user@$DEPLOY_TEST_HOST"
deploy_dir="/home/$deploy_user/deploy"
previous_deploy_dir="$deploy_dir/previous"
latest_deploy_dir="$deploy_dir/latest"

echo "Deploying tests to a $remote_host host, preparing deploy directories.."

ssh ${remote_host} "rm -r -f $previous_deploy_dir;
     mkdir -p $latest_deploy_dir;
     cp -r $latest_deploy_dir $previous_deploy_dir;"

echo
echo "Dirs prepared, copying package, this can take a while..."

scp  execute_load_test.py ${remote_host}:${latest_deploy_dir}/execute_load_test.py