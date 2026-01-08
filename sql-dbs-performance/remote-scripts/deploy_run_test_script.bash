#!/bin/bash
set -euo pipefail

remote_host="ops@${DEPLOY_HOST}"
tests_dir="/home/ops/deploy/tests"

ssh $remote_host "mkdir -p $tests_dir"
scp ../run_test.py "${remote_host}:${tests_dir}/run_test.py"