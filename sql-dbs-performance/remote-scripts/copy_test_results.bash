#!/bin/bash
set -euo pipefail

remote_host="ops@${TESTS_HOST}"
tests_dir="/home/ops/deploy/tests"
scp "${remote_host}:${tests_dir}/*.txt" "../results/remote/"