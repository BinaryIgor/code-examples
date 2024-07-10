#!/bin/bash
set -euo pipefail

cd ..
. config.env
cd scripts

app="load-test"
deploy_user="deploy"
test_host="test-fra.$ROOT_DOMAIN"
remote_host="$deploy_user@$test_host"
deploy_dir="/home/$deploy_user/deploy/$app"
test_results_dir="/home/$deploy_user/load-test-results"

echo "Deploying $app to a $remote_host host, preparing directories.."

ssh -oStrictHostKeyChecking=accept-new ${remote_host} "rm -r -f $deploy_dir;
     mkdir -p $deploy_dir;
     mkdir -p $test_results_dir"

echo
echo "Dirs prepared, copying package, this can take a while..."

cd ..
scp -r $app/dist/* ${remote_host}:${deploy_dir}

echo
echo "Load test deployed, go and run it!"