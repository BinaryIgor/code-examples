#!/bin/bash
docker build . -t some-wisdom-app

docker rm some-wisdom-app

docker run -p 8080:8080 --name some-wisdom-app some-wisdom-app