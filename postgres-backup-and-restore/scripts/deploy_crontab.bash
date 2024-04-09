#!/bin/bash
set -eu

deploy_user="deploy"
remote_host="$deploy_user@$DEPLOY_HOST"
crontab_path="/home/deploy/crontab.txt"

scp ../crontab.txt ${remote_host}:${crontab_path}
ssh ${remote_host} "echo 'current crontab:'
crontab -l
echo
echo "Updating it from ${crontab_path}..."
crontab ${crontab_path}
echo
echo 'Crontab updated, new state:'
crontab -l"