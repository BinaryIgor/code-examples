#!/bin/bash
export TEST_CASE="BATCH_INSERTS"
export QUERIES_TO_EXECUTE=1000
export QUERIES_RATE=20
export QUERIES_BATCH_SIZE=1000
bash run_test_case.bash
