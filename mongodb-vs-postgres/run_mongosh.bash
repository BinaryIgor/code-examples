#!/bin/bash
# "mongodb://user:pass@127.0.0.1:27017/experiments" 
docker run --network host  -v /tmp/data:/tmp/data -it --rm mongo:8.2 \
  mongosh -u user -p pass