#!/bin/bash
set -euo pipefail

app="load-test"
deploy_user="test-machine"
remote_host="$deploy_user@$TEST_HOST"
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