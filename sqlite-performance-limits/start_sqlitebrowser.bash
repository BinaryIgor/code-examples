#!/bin/bash

container_name="sqlitebrowser"
volume_dir="${SQLITE_TESTS_VOLUME_DIR:-${HOME}/sqlite-limits-tests_volume}"

docker stop $container_name
docker rm $container_name

# ref: https://hub.docker.com/r/linuxserver/sqlitebrowser

docker run -d --name=sqlitebrowser \
  -p 3000:3000 -p 3001:3001 \
  -v "$volume_dir:/config" \
  lscr.io/linuxserver/sqlitebrowser:3.12.2