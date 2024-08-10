#!/bin/bash

echo "Building jar..."
echo

mvn clean install

echo
echo "Jar built, preparing Docker image..."

container='analytics-app'

docker build . -t ${container}

docker stop ${container}
docker rm ${container}

docker run --network host \
  -e "JDBC_URL=jdbc:postgresql://localhost:5432/analytics" \
  -e "DB_USERNAME=analytics" \
  -e "DB_PASSWORD=analytics" \
  --memory "2000M" --cpus "4" --name ${container} ${container}