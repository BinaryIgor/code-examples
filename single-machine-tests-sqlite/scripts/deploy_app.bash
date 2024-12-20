#!/bin/bash
set -euo pipefail

echo "Deploying single app..."

echo "Single app..."
export APP="single-app"
bash deploy.bash
echo

echo "App is deployed!"