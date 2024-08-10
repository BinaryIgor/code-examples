#!/bin/bash
container='analytics-db'

docker build . -t ${container}

docker stop ${container}
docker rm ${container}

docker run --network host --memory "1000M" --cpus "2" --name ${container} ${container}