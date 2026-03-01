#!/bin/bash
docker run --network host  -v /tmp/data:/tmp/data -it --rm mongo:7.0.29 \
  mongosh "mongodb://json:json@localhost:27017/json?authSource=admin"