#!/bin/bash
set -e

cd ..
cd shared

echo "Building shared module..."
echo
mvn clean install
echo

cd ..
cd project

echo "Building project service..."
echo
mvn clean package -Pexecutable
echo

docker build . -t modular-pattern-project-service

echo
echo "project service is built and ready to run!"