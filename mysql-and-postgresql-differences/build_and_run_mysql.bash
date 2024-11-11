#!/bin/bash

cd mysql

container_name="mysql-differences-db"
volume_dir="${MYSQL_VOLUME_DIR:-${HOME}/${container_name}_volume}"

docker build . -t $container_name

docker stop $container_name
docker rm $container_name

docker run -d -v "${volume_dir}:/var/lib/mysql" -p "3306:3306" \
  -e "MYSQL_ROOT_PASSWORD=mysql" \
  --memory "4G" --cpus "2" \
  --name $container_name $container_name
