#!/bin/bash
set -euo pipefail

app="static-app"
app_dist_dir="dist/$REGION"
deploy_user="deploy"
remote_host="$deploy_user@$DEPLOY_HOST"
deploy_dir="/home/$deploy_user/deploy/$app"
previous_deploy_dir="$deploy_dir/previous"
latest_deploy_dir="$deploy_dir/latest"

echo "Deploying $app to a $remote_host host, from $app_dist_dir dir, preparing deploy directories.."

ssh -oStrictHostKeyChecking=accept-new ${remote_host} "rm -r -f $previous_deploy_dir;
     mkdir -p $latest_deploy_dir;
     cp -r $latest_deploy_dir $previous_deploy_dir;
     rm -r -f $latest_deploy_dir;
     mkdir -p $latest_deploy_dir;"

echo
echo "Dirs prepared, copying package, this can take a while..."

cd ..
scp -r "$app/$app_dist_dir"/* ${remote_host}:${latest_deploy_dir}

echo
echo "Package copied, loading and running app, this can take a while.."

ssh ${remote_host} "cd $latest_deploy_dir; bash load_and_run_app.bash"

echo
echo "App loaded, checking its logs and status after 5s.."
sleep 5
echo

ssh ${remote_host} "docker logs $app"
echo
echo "App status:"
ssh ${remote_host} "docker container inspect -f '{{ .State.Status }}' $app"

echo "App deployed!"
echo "In case of problems you can rollback to previous deployment: $previous_deploy_dir"