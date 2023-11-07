#!/bin/bash

cd ..

docker build --network=host . -t modular-monolith-with-independent-deployments

docker rm modular-monolith-with-independent-deployments

exec docker run modular-monolith-with-independent-deployments