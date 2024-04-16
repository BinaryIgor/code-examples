#!/bin/bash
set -eu

export NEW_ROOT_PASSWORD="postgres"
export APP_DB_PASSWORD="htmx_db_password"

bash init_db.bash