#!/bin/bash

cd mysql

container_name="the-order-of-data-mysql"
volume_dir="${MYSQL_VOLUME_DIR:-${HOME}/${container_name}_volume}"

docker build . -t $container_name

docker stop $container_name
docker rm $container_name

docker run -d -v "${volume_dir}:/var/lib/mysql" --network host \
  -e "MYSQL_ROOT_PASSWORD=mysql" \
  -e "MYSQL_DATABASE=mysql" \
  --memory "4G" --cpus "2" \
  --name $container_name $container_name
