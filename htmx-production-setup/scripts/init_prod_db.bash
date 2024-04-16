#!/bin/bash
set -euo pipefail

cd ..
. config_prod.env

cd scripts

script_to_execute="
export SECRETS_PATH=$SECRETS_PATH
bash /tmp/init_prod_db.bash"

echo "$script_to_execute"

remote_host="$DEPLOY_USER@$DEPLOY_HOST"

scp ../db/init_prod_db.bash $remote_host:/tmp/init_prod_db.bash
ssh $remote_host $script_to_execute