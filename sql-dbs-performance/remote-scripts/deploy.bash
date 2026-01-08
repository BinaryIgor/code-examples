#!/bin/bash
set -euo pipefail

app=$APP
app_dir=${APP_DIR:-"$app"}
deploy_user="ops"
remote_host="$deploy_user@$DEPLOY_HOST"
deploy_dir="/home/$deploy_user/deploy/$app"
previous_deploy_dir="$deploy_dir/previous"
latest_deploy_dir="$deploy_dir/latest"
skip_running=${SKIP_RUNNING:-false}

echo "Deploying $app to $remote_host host, preparing deploy directories under $deploy_dir path..."

ssh -oStrictHostKeyChecking=accept-new "${remote_host}" "
rm -r -f $previous_deploy_dir;
mkdir -p $latest_deploy_dir;
cp -r $latest_deploy_dir $previous_deploy_dir;"

echo
echo "Dirs prepared, copying package - this can take a while..."

cd ..

scp -r ${app_dir}/dist/* "${remote_host}:${latest_deploy_dir}"

if [ "$skip_running" = true ]; then
  echo
  echo "SKIP_RUNNING is set to true, skipping running app, loading it, this might take a while..."

  ssh "${remote_host}" "cd ${latest_deploy_dir}; bash load_app.bash"

  echo
  echo "App is copied & loaded and ready to be run manually!"
else
  echo
  echo "Package copied, loading and running app, this might take a while..."

  ssh "${remote_host}" "cd ${latest_deploy_dir}; bash load_and_run_app.bash"

  echo
  echo "App loaded, checking its logs and status after 5s..."
  sleep 5
  echo

  ssh "${remote_host}" "docker logs ${app}"
  echo
  echo "App status:"
  ssh "${remote_host}" "docker container inspect -f '{{ .State.Status }}' ${app}"

  echo "App deployed"
  echo "In case of problems, you can rollback to the previous deployment: $previous_deploy_dir"
fi
