#!/bin/bash
set -e

source modular_monolith_db.env

cd ..

docker rm modular-monolith-with-independent-deployments || true

docker build -f DockerfileRun . -t modular-monolith-with-independent-deployments

exec docker run --network=host \
  -e BUDGET_DB_URL -e BUDGET_DB_USERNAME -e BUDGET_DB_PASSWORD \
  -e CAMPAIGN_DB_URL -e CAMPAIGN_DB_USERNAME -e CAMPAIGN_DB_PASSWORD \
  -e INVENTORY_DB_URL -e INVENTORY_DB_USERNAME -e INVENTORY_DB_PASSWORD \
  -v "modular-monolith-with-independent-deployments-build-volume:/app" \
  --name modular-monolith-with-independent-deployments modular-monolith-with-independent-deployments