#!/bin/bash
set -euo pipefail

cwd=$PWD
cd ..
. "config_prod.env"
cd $cwd

app=$APP
app_container="htmx-production-setup-$app"
app_dir="${APP_DIR:-$APP}"
remote_host="$DEPLOY_USER@$DOMAIN"
deploy_dir="$DEPLOY_DIR/$app"
previous_deploy_dir="$deploy_dir/previous"
latest_deploy_dir="$deploy_dir/latest"

echo "Deploying $app to a $remote_host host, preparing deploy directories..."

ssh ${remote_host} "rm -rf $previous_deploy_dir
mkdir -p $latest_deploy_dir
cp -r $latest_deploy_dir $previous_deploy_dir
rm -r $latest_deploy_dir
mkdir $latest_deploy_dir"

echo
echo "Dirs prepared, copying package, this can take a while..."

cd ..
scp -r $app_dir/dist/* ${remote_host}:${latest_deploy_dir}

echo
echo "Package copied, loading and running app, this can take a while..."

ssh ${remote_host} "cd $latest_deploy_dir; bash load_and_run_app.bash"

echo
echo "App loaded, checking its logs and status after 5s..."
sleep 5
echo

ssh ${remote_host} "docker logs $app_container"
echo
echo "App status:"
ssh ${remote_host} "docker container inspect -f '{{ .State.Status }}' $app_container"

echo "App deployed!"
echo "In case of problems you can rollback to the previous deployment: $previous_deploy_dir"