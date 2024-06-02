#!/bin/bash
set -e

skip_dependencies=${SKIP_DEPENDENCIES:-false}
only_user_module=${ONLY_USER_MODULE:-false}

if [ $skip_dependencies == 'true' ]; then
  echo "Skipping dependencies and assuming that they are prepared!"
else
  cd ..
  cd shared

  echo "Building shared module..."
  echo
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
fi

cd ..
cd modular-monolith
echo "Building modular monolith..."
if [ $only_user_module == 'true' ]; then
  echo "...with only user module"
  echo
  mvn clean package -P=userModule,executable
else
  echo
  mvn clean package -P=allModules,executable
fi
echo

docker build . -t modular-pattern-monolith
echo

echo "Modular monolith is built and ready to run!"