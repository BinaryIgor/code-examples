#!/bin/bash

container="postgres-db"

docker build . -t $container

docker rm $container

docker run -d --memory "500M" --cpus "1" -p "5432:5432" --name $container $container
