#!/bin/bash
cd tests
mvn clean package
docker build . -t "sqlite-limits-tests"