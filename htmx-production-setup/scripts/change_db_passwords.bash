#!/bin/bash
set -euo pipefail

cd ..
. config_prod.env

cd scripts

script_to_execute="
export SECRETS_PATH=$SECRETS_PATH
bash /tmp/change_prod_db_passwords.bash"

echo "$script_to_execute"

remote_host="$DEPLOY_USER@$DEPLOY_HOST"

scp ../db/change_prod_db_passwords.bash $remote_host:/tmp/change_prod_db_passwords.bash
ssh $remote_host $script_to_execute