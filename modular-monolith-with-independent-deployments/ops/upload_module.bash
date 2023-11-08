#!/bin/bash
set -e

module=$1

if [ -z $module ]; then
  echo "Single argument with module name is required"
  exit 1
fi

cd ../modules
if [ ! -d $module ]; then
  echo "$module module doesn't exist"
  exit 1
fi

cd $module

echo "Building and upload $module to local nexus repo..."
mvn clean deploy