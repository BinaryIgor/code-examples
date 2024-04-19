#!/bin/bash
set -eu

new_root_db_password=$(cat db-root-password.txt)
app_db_user="htmx_app"
app_db_name="htmx_app"
app_db_password=$(cat db-password.txt)

connect_to_db="docker exec -it htmx-production-setup-db psql -U postgres -d postgres -c"
$connect_to_db "ALTER USER postgres WITH password '$new_root_db_password'"
$connect_to_db "ALTER USER ${app_db_user} WITH password '$app_db_password'"
