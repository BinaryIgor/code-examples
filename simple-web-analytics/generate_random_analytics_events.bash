#!/bin/bash
container='analytics-events-generator'

docker build . -t ${container}

docker stop ${container}
docker rm ${container}

docker run --network host \
  -e "SPRING_PROFILES_ACTIVE=events-generator" \
  -e "EVENTS_SIZE=100000" \
  -e "EVENTS_CONCURRENCY=1000" \
  --memory "1000M" --cpus "4" --name ${container} ${container}
