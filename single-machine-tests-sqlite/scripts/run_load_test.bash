#!/bin/bash
set -euo pipefail

tested_host="http://$TESTED_HOST:80"
tested_machine_name=$TESTED_MACHINE_NAME
deploy_user="test-machine"
load_test_dir="/home/$deploy_user/deploy/load-test"
test_results_dir="/home/$deploy_user/load-test-results"
test_profile="${TEST_PROFILE:-low_load}"
test_results_file="${test_results_dir}/test_results-${tested_machine_name}-${test_profile}.txt"
test_hosts=${TEST_HOSTS}
test_hosts_size=0

for h in ${test_hosts}; do
    test_hosts_size=$((test_hosts_size + 1))
done

echo "Running load test on $test_hosts hosts..."
echo

for h in ${test_hosts}; do
    echo "Running load test on $h host with $test_profile test profile..."
    
    remote_host="$deploy_user@$h"
    ssh ${remote_host} "cd $load_test_dir; 
    export HOST=$tested_host
    export TEST_RESULTS_INSTANCES=$test_hosts_size
    export TEST_PROFILE=$test_profile
    bash load_and_run_app.bash &> $test_results_file &"
    
    echo
    echo "load test on $h is running! To check out results, do (after a few seconds):"
    echo "ssh $remote_host cat $test_results_file"
    echo
done