#!/bin/bash

container_name="modular-monolith-with-independent-deployments-db"

docker build . -t $container_name

docker rm $container_name

docker run -p "5555:5432" --name $container_name $container_name
