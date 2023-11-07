#!/bin/bash

docker build . -t nexus

docker rm nexus

docker run -p 8081:8081 --name nexus nexus