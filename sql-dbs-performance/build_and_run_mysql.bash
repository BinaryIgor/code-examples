#!/bin/bash

cd mysql

container_name="mysql-performance"
volume_dir="${MYSQL_VOLUME_DIR:-${HOME}/${container_name}_volume}"

docker build . -t $container_name

docker stop $container_name
docker rm $container_name

# innodb_buffer_pool_size - caches data in memory instead of disk to limit I/O (should be 50-80% of RAM)
# innodb_redo_log_capacity - reduces frequency of checkpoint operations
# transaction isolation is set to a lower than default level (REPEATABLE-READ) to make comparison with Postgres fair
docker run -d -v "${volume_dir}:/var/lib/mysql" --network host \
  -e "MYSQL_ROOT_PASSWORD=performance" \
  -e "MYSQL_DATABASE=performance" \
  --memory "16G" --cpus "8" --shm-size="1G" \
  --name $container_name $container_name \
  --innodb_buffer_pool_size=12G \
  --innodb_redo_log_capacity=2G \
  --transaction-isolation='READ-COMMITTED'
