#!/bin/bash
set -eu

new_root_password=${NEW_ROOT_PASSWORD}
app_db_user="htmx_app"
app_db_name="htmx_db"
app_db_password=${APP_DB_PASSWORD}

connect_to_db="docker exec -it htmx-production-setup-db psql -U postgres -d postgres -c"

$connect_to_db "ALTER USER postgres WITH password '$new_root_password'"

$connect_to_db "CREATE USER ${app_db_user}"
$connect_to_db "ALTER USER ${app_db_user} WITH password '$app_db_password'"

$connect_to_db "CREATE DATABASE ${app_db_name}"
$connect_to_db "ALTER DATABASE ${app_db_name} OWNER TO ${app_db_user}"
