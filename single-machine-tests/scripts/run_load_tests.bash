#!/bin/bash
set -euo pipefail

single_machine_host="http://$SINGLE_MACHINE_HOST:80"
deploy_user="test-machine"
load_test_dir="/home/$deploy_user/deploy/load-test/latest"
test_profile="${TEST_PROFILE:-LOW_LOAD}"
in_memory_endpoint="${IN_MEMORY_ENDPOINT:-false}"
test_results_file="test_results-${test_profile}.txt"

echo "Running load tests to $TEST_HOSTS..."

for h in ${TEST_HOSTS[@]}; do
    echo "Running load test on $h host with $test_profile test profile..."
    
    remote_host="$deploy_user@$h"
    ssh ${remote_host} "cd $load_test_dir; 
    export HOST=$single_machine_host
    export TEST_PROFILE=$test_profile
    export IN_MEMORY_ENDPOINT=$in_memory_endpoint
    bash load_and_run_app.bash &> $test_results_file &"
    
    echo "Load test on $h are running, checkout $load_test_dir/$test_results_file for results!"
    echo
done