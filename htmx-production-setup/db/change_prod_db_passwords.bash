#!/bin/bash
set -eu

export NEW_ROOT_DB_PASSWORD=$(cat $SECRETS_PATH/db-root-password.txt)
export DB_PASSWORDd=$(cat $SECRETS_PATH/db-password.txt)

bash change_db_passwords.bash