#!/bin/bash

container="metrics-and-logs-collector"

docker rm $container

docker build . -t $container

docker run -d --memory "250M" --cpus "1" --network host \
  --volume /var/run/docker.sock:/var/run/docker.sock \
  --name $container $container 