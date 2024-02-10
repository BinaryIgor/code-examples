#!/bin/bash

container="some-custom-app"

docker rm $container

docker build . -t $container

docker run -d \
  --name $container $container