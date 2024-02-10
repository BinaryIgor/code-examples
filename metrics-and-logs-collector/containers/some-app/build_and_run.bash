#!/bin/bash

container="some-app"

docker rm $container

docker build . -t $container

docker run -d --memory "100M" --cpus "1" \
  --name $container $container