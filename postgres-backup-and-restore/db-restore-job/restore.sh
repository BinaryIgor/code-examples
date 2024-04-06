#!/bin/sh
set -u

db_user=$DB_USER
db_name=$DB_NAME
backup_path="/backups/backup_restore.back"

echo "$(date -Iseconds): restoring ${db_name} db with ${db_user} user from ${backup_path} file..."
echo

# Assuming compressed, custom dump format. For details, check out the docs: https://www.postgresql.org/docs/current/app-pgrestore.html
pg_restore -v -Fc -h "0.0.0.0" -U $db_user -d $db_name "$backup_path"

echo
echo "$(date -Iseconds): db restored!"