#!/bin/bash
set -euo pipefail

cd ..
. config.env
cd scripts

export TEST_HOST="test-fra.$ROOT_DOMAIN"
export TESTED_HOST="static-tor.$ROOT_DOMAIN"

bash run_load_test.bash