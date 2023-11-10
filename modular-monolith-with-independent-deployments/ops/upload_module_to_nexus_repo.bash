#!/bin/bash
set -e

module=$1

if [ -z $module ]; then
  echo "Single argument with module name is required"
  exit 1
fi

cd ..

full_module_path="$PWD/$module"

if [ ! -d $full_module_path ]; then
  echo "$full_module_path module doesn't exist"
  exit 1
fi

cd $full_module_path

echo "Building and upload $full_module_path to local nexus repo..."
mvn clean deploy