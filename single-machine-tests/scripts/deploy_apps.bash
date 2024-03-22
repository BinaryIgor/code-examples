#!/bin/bash
set -euo pipefail

echo "Deploying single db and app..."

echo "Single db..."
export APP=single-db
bash deploy.bash
echo

echo "Single app..."
export APP="single-app"
bash deploy.bash
echo

echo "Apps are deployed!"