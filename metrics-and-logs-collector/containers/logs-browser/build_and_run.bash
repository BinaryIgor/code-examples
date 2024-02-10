#!/bin/bash

container="logs-browser"

docker build . -t $container

docker rm $container

docker run -d --memory "250M" --cpus "0.25" -p "11111:8080" \
    --volume "/tmp/metrics-and-logs-collector/logs:/usr/share/nginx/site:ro" \
    --name $container $container