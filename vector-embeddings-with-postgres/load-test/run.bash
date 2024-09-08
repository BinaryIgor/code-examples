#!/bin/bash
set -eu

export HOST="http://localhost:8080"
export REQUESTS=100
export REQUESTS_PER_SECOND=10
export MAX_CONCURRENCY=20

java LoadTest.java