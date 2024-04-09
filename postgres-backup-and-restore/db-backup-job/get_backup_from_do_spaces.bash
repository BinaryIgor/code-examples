#!/bin/bash
set -eu

# To use it, go to DigitalOcean Spaces UI.
# Find desired backup and click "Quick Share".
# It allows to generate tmp urls to download files from private spaces ;)
export DO_SPACE_BACKUP_URL="generated-url"
curl -o "/home/deploy/db-backups/backup_restore.back" ${DO_SPACE_BACKUP_URL}