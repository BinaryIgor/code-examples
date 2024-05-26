#!/bin/bash

cd ..
cd shared

echo "Building shared module..."
mvn clean install
echo

cd ..
cd user

echo "Building user module..."
echo
mvn clean install
echo

cd ..
cd project

echo "Building project module..."
echo
mvn clean install
echo

cd ..
cd modular-monolith
echo "Building modular monolith..."
echo
mvn clean package -Pexecutable
echo
echo "Modular monolith is built and ready to run!"