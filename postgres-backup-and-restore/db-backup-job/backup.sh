#!/bin/sh
set -eu

started_at=$(date +%s)
echo $started_at > "/job-metrics/db-backup__last-started-at.txt"

db_user=$DB_USER
db_name=$DB_NAME
backup_date=$(date '+%Y%m%d_%H%M%S')
backup_name="backup_${backup_date}.back"
backup_path="/backups/$backup_name"
max_local_backups=$MAX_LOCAL_BACKUPS
max_do_backups=$MAX_DO_BACKUPS

echo "$(date -Iseconds): about to backup ${db_name} db with ${db_user} user to ${backup_path} file..."
echo
# Compressed, custom dump format. For details, check out the docs: https://www.postgresql.org/docs/current/app-pgdump.html
pg_dump -v -Fc -Z 4 -h "0.0.0.0" -U $db_user -d $db_name > "$backup_path"

echo
echo "$(date -Iseconds): backup done! Checking if we need to remove old backups locally..."

export BACKUPS_PATH="/backups"

echo
export MAX_BACKUPS=$max_local_backups
python3 remove_old_backups.py
echo

if [ "${UPLOAD_TO_DO_SPACES:-false}" = "true" ]; then
  echo "$(date -Iseconds): Uploading backup to DO spaces..."
  echo
  export BACKUP_LOCAL_PATH=$backup_path
  export BACKUP_NAME=$backup_name
  export MAX_BACKUPS=$max_do_backups
  python3 upload_backup_to_do_spaces.py
  echo
  echo "$(date -Iseconds): Backups uploaded to DO spaces!"
else
  echo "$(date -Iseconds): Skipping backup to DO spaces upload"
fi

ended_at=$(date +%s)
echo $ended_at > "/job-metrics/db-backup__last-ended-at.txt"

echo "Backup job is done!"
# Just for the logs aesthetics
echo