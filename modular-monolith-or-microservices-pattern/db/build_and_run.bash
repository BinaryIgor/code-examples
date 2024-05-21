#!/bin/bash

container_name="modular-pattern-db"

docker build . -t $container_name

docker rm $container_name

docker run -p "5432:5432" --name $container_name $container_name
