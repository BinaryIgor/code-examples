#!/bin/bash
set -euo pipefail

echo "Deploying load tests to $TEST_HOSTS..."

idx=0
for h in ${TEST_HOSTS[@]}; do
    echo "Deploy load test to $h host..."
    
    export TEST_HOST="$h"
    bash deploy_load_test.bash &
    d_pids[$idx]=$!
    idx=$((idx+1))

    echo "Load test is being deployed to $h host!"
done

while true; do
    echo "Waiting for deployments to finish..."
    echo
    
    pending_deployments=0
    for pid in ${d_pids[@]}; do
        if [ -n "$(ps -p $pid -o pid=)" ]; then
            echo "Deployment of $pid pid is still running..."
            pending_deployments=$((pending_deployments + 1))
        fi
    done

    if (( $pending_deployments > 0 )); then
        echo "There are still $pending_deployments pending deployments, waiting..."
        echo
        sleep 5
    else
        echo "All deployments have finished, exiting!"
        break
    fi
done

