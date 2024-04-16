#!/bin/bash
set -eu

export NEW_ROOT_PASSWORD=$(cat $SECRETS_PATH/db-root-password.txt)
export APP_DB_PASSWORDd=$(cat $SECRETS_PATH/db-app-password.txt)

bash init_db.bash