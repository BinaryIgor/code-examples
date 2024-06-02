#!/bin/bash
set -e

cd ..
cd shared

echo "Building shared module..."
echo
mvn clean install
echo

cd ..
cd user

echo "Building user service..."
echo
mvn clean package -Pexecutable
echo

docker build . -t modular-pattern-user-service

echo
echo "user service is built and ready to run!"