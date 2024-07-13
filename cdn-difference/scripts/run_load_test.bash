#!/bin/bash
set -euo pipefail

tested_host="https://$TESTED_HOST"
deploy_user="deploy"
load_test_dir="/home/$deploy_user/deploy/load-test"
test_results_dir="/home/$deploy_user/load-test-results"
test_results_file="${test_results_dir}/test_results-${TESTED_HOST}.txt"
test_host=$TEST_HOST

echo "Running load test for $tested_host on $test_host host..."

remote_host="$deploy_user@$test_host"
ssh ${remote_host} "cd $load_test_dir; 
export HOST=$tested_host
bash load_and_run_app.bash &> $test_results_file &"

echo
echo "load test on $test_host is running! To check out results, do (after a few seconds):"
echo "ssh $remote_host cat $test_results_file"
echo