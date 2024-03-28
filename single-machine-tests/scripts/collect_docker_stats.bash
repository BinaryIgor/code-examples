#!/bin/bash
set -euo pipefail

stats_file=${STATS_FILE:-docker_stats.txt}
collection_interval=${COLLECTION_INTERVAL:-20}

echo "Removing previous stats file, if exists..."

rm -f $stats_file

echo

while true; do
  echo "Collecting docker stats to $stats_file..."

  date=$(date --utc +%FT%TZ)
  echo "Date: $date" >> $stats_file
  docker stats --no-stream >> $stats_file
  echo "" >> $stats_file

  echo "Stats collected, sleeping for $collection_interval s..."
  echo "..."
  sleep $collection_interval;
done