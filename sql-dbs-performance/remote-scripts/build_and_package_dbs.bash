#!/bin/bash
set -euo pipefail

echo "Building dbs..."

cd ..

echo "Building mysql..."
app="mysql"
cd "$app"
bash build_and_package.bash
cd ..

echo
echo "mysql built! Building postgresql..."
app="postgresql"
cd "$app"
bash build_and_package.bash
cd ..

echo
echo "postgresql built! Building mariadb..."
app="mariadb"
cd "$app"
bash build_and_package.bash
cd ..

echo
echo "dbs are built and ready to deploy!"