#!/bin/bash
set -euo pipefail

app="load-test"
deploy_user="test-machine"
remote_host="$deploy_user@$DEPLOY_TEST_HOST"
deploy_dir="/home/$deploy_user/deploy/$app"
previous_deploy_dir="$deploy_dir/previous"
latest_deploy_dir="$deploy_dir/latest"

echo "Deploying $app to a $remote_host host, preparing deploy directories.."

ssh ${remote_host} "rm -r -f $previous_deploy_dir;
     mkdir -p $latest_deploy_dir;
     cp -r $latest_deploy_dir $previous_deploy_dir;"

echo
echo "Dirs prepared, copying package, this can take a while..."

cd ..
scp -r $app/dist/* ${remote_host}:${latest_deploy_dir}

echo
echo "Load test deployed, go and run it!"