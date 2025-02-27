#!/bin/bash
set -euo pipefail

echo "Deploying apps..."

echo "react...c
cd ../react
bash deploy.bash
echo

echo "react deployed! Deploying nginx..."

cd ../scripts
export APP=nginx
bash deploy_app.bash
echo

echo "nginx deployed! Time for the server..."
export APP=server
bash deploy_app.bash
echo

echo "All apps have been deployed!"
