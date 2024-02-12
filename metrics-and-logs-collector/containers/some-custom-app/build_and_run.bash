#!/bin/bash

container="some-custom-app"

docker rm $container

docker build . -t $container

# BUSY_CPUS specifies how many processes we will start that will make given number of cpus busy 100% of the time.
# For details see some_custom_app.py
docker run -d -e "BUSY_CPUS=3" --name $container $container